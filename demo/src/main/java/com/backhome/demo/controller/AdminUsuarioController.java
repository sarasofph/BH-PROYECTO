package com.backhome.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.AdministradorRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;

    public AdminUsuarioController(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
    }

    // =========================================================
    // LISTADO DE USUARIOS
    // =========================================================

    @GetMapping
    public String usuarios(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String estado,
            Model model) {

        String texto = buscar == null
                ? ""
                : buscar.trim();

        List<Persona> usuarios;

        if (estado != null
                && !estado.trim().isEmpty()
                && !estado.equalsIgnoreCase("todos")) {

            try {

                EstadoPersona estadoSeleccionado =
                        EstadoPersona.valueOf(
                                estado.toLowerCase()
                        );

                usuarios =
                        personaRepository.buscarPorTextoYEstado(
                                texto,
                                estadoSeleccionado
                        );

            } catch (IllegalArgumentException e) {

                usuarios =
                        personaRepository.buscarPorTexto(texto);
            }

        } else {

            usuarios =
                    personaRepository.buscarPorTexto(texto);
        }

        // =====================================================
        // MAPA DE ROLES
        // =====================================================

        Map<Integer, String> roles = new HashMap<>();

        for (Persona persona : usuarios) {

            Integer id = persona.getIdPersona();

            if (administradorRepository
                    .existsByPersona_IdPersona(id)) {

                roles.put(id, "ADMIN");

            } else if (clienteRepository
                    .existsByPersona_IdPersona(id)) {

                roles.put(id, "CLIENTE");

            } else {

                roles.put(id, "SIN PERFIL");
            }
        }

        // =====================================================
        // ESTADÍSTICAS
        // =====================================================

        long totalUsuarios =
                personaRepository.count();

        long usuariosActivos =
                personaRepository.countByEstado(
                        EstadoPersona.activo
                );

        long usuariosBloqueados =
                personaRepository.countByEstado(
                        EstadoPersona.bloqueado
                );

        long usuariosSuspendidos =
                personaRepository.countByEstado(
                        EstadoPersona.suspendido
                );

        model.addAttribute(
                "usuarios",
                usuarios
        );

        model.addAttribute(
                "roles",
                roles
        );

        model.addAttribute(
                "totalUsuarios",
                totalUsuarios
        );

        model.addAttribute(
                "usuariosActivos",
                usuariosActivos
        );

        model.addAttribute(
                "usuariosBloqueados",
                usuariosBloqueados
        );

        model.addAttribute(
                "usuariosSuspendidos",
                usuariosSuspendidos
        );

        model.addAttribute(
                "buscar",
                texto
        );

        model.addAttribute(
                "estadoSeleccionado",
                estado == null ? "todos" : estado
        );

        model.addAttribute(
                "estados",
                EstadoPersona.values()
        );

        return "admin/usuarios";
    }

    // =========================================================
    // EDITAR USUARIO
    // =========================================================

    @PostMapping("/{id}/editar")
    public String editarUsuario(
            @PathVariable Integer id,
            @RequestParam String tipoDocumentoId,
            @RequestParam String numeroDocumento,
            @RequestParam String primerNombre,
            @RequestParam(required = false) String segundoNombre,
            @RequestParam String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam String email,
            @RequestParam String numeroTel,
            RedirectAttributes redirectAttributes) {

        Persona persona =
                personaRepository.findById(id)
                        .orElse(null);

        if (persona == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        // =====================================================
        // VALIDACIONES
        // =====================================================

        String emailNormalizado =
                email == null
                        ? ""
                        : email.trim().toLowerCase();

        String documentoNormalizado =
                numeroDocumento == null
                        ? ""
                        : numeroDocumento.trim();

        if (emailNormalizado.isEmpty()
                || documentoNormalizado.isEmpty()
                || primerNombre == null
                || primerNombre.trim().isEmpty()
                || primerApellido == null
                || primerApellido.trim().isEmpty()
                || numeroTel == null
                || numeroTel.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Completa todos los campos obligatorios."
            );

            return "redirect:/admin/usuarios";
        }

        // =====================================================
        // EMAIL DUPLICADO
        // =====================================================

        Persona personaConEmail =
                personaRepository
                        .findByEmailIgnoreCase(
                                emailNormalizado
                        )
                        .orElse(null);

        if (personaConEmail != null
                && !personaConEmail
                        .getIdPersona()
                        .equals(id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El correo electrónico ya está registrado."
            );

            return "redirect:/admin/usuarios";
        }

        // =====================================================
        // DOCUMENTO DUPLICADO
        // =====================================================

        if (!documentoNormalizado.equals(
                persona.getNumeroDocumento())) {

            if (personaRepository
                    .existsByNumeroDocumento(
                            documentoNormalizado
                    )) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "El número de documento ya está registrado."
                );

                return "redirect:/admin/usuarios";
            }
        }

        // =====================================================
        // ACTUALIZAR
        // =====================================================

        persona.setTipoDocumentoId(
                tipoDocumentoId.trim()
        );

        persona.setNumeroDocumento(
                documentoNormalizado
        );

        persona.setPrimerNombre(
                primerNombre.trim()
        );

        persona.setSegundoNombre(
                limpiarCampo(segundoNombre)
        );

        persona.setPrimerApellido(
                primerApellido.trim()
        );

        persona.setSegundoApellido(
                limpiarCampo(segundoApellido)
        );

        persona.setEmail(
                emailNormalizado
        );

        persona.setNumeroTel(
                numeroTel.trim()
        );

        persona.setUpdatedAt(
                LocalDateTime.now()
        );

        personaRepository.save(persona);

        redirectAttributes.addFlashAttribute(
                "success",
                "El usuario fue actualizado correctamente."
        );

        return "redirect:/admin/usuarios";
    }

    // =========================================================
    // CAMBIAR ESTADO
    // =========================================================

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes) {

        Persona persona =
                personaRepository.findById(id)
                        .orElse(null);

        if (persona == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        EstadoPersona nuevoEstado;

        try {

            nuevoEstado =
                    EstadoPersona.valueOf(
                            estado.toLowerCase()
                    );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El estado seleccionado no es válido."
            );

            return "redirect:/admin/usuarios";
        }

        persona.setEstado(nuevoEstado);

        persona.setUpdatedAt(
                LocalDateTime.now()
        );

        personaRepository.save(persona);

        String mensaje;

        switch (nuevoEstado) {

            case activo:
                mensaje =
                        "El usuario fue activado correctamente.";
                break;

            case bloqueado:
                mensaje =
                        "El usuario fue bloqueado correctamente.";
                break;

            case suspendido:
                mensaje =
                        "El usuario fue suspendido correctamente.";
                break;

            default:
                mensaje =
                        "El estado del usuario fue actualizado.";
        }

        redirectAttributes.addFlashAttribute(
                "success",
                mensaje
        );

        return "redirect:/admin/usuarios";
    }

    // =========================================================
    // MÉTODO AUXILIAR
    // =========================================================

    private String limpiarCampo(String valor) {

        if (valor == null) {
            return null;
        }

        String limpio = valor.trim();

        return limpio.isEmpty()
                ? null
                : limpio;
    }
}