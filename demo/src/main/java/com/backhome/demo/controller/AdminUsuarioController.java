package com.backhome.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.Administrador;
import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.model.Persona;
import com.backhome.demo.model.TipoDocumento;
import com.backhome.demo.repository.AdministradorRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;
import com.backhome.demo.repository.TipoDocumentoRepository;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUsuarioController(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository,
            TipoDocumentoRepository tipoDocumentoRepository,
            PasswordEncoder passwordEncoder) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // LISTAR USUARIOS
    // =========================================================

    @GetMapping
    public String listarUsuarios(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rol,
            Model model) {

        List<Persona> todosLosUsuarios =
                personaRepository.findAllByOrderByIdPersonaDesc();

        Map<Integer, String> roles =
                construirMapaRoles();

        String textoBusqueda =
                buscar == null
                        ? ""
                        : buscar.trim().toLowerCase();

        List<Persona> usuarios =
                new ArrayList<>();

        for (Persona usuario : todosLosUsuarios) {

            String nombreCompleto =
                    construirNombreCompleto(usuario)
                            .toLowerCase();

            String documento =
                    usuario.getNumeroDocumento() == null
                            ? ""
                            : usuario.getNumeroDocumento()
                                    .toLowerCase();

            String email =
                    usuario.getEmail() == null
                            ? ""
                            : usuario.getEmail()
                                    .toLowerCase();

            String telefono =
                    usuario.getNumeroTel() == null
                            ? ""
                            : usuario.getNumeroTel()
                                    .toLowerCase();

            boolean coincideBusqueda =
                    textoBusqueda.isEmpty()
                    || nombreCompleto.contains(textoBusqueda)
                    || documento.contains(textoBusqueda)
                    || email.contains(textoBusqueda)
                    || telefono.contains(textoBusqueda);

            boolean coincideEstado = true;

            if (estado != null && !estado.isBlank()) {

                coincideEstado =
                        usuario.getEstado() != null
                        && usuario.getEstado()
                                .name()
                                .equalsIgnoreCase(estado);
            }

            String rolUsuario =
                    roles.getOrDefault(
                            usuario.getIdPersona(),
                            "Sin rol"
                    );

            boolean coincideRol = true;

            if (rol != null && !rol.isBlank()) {

                coincideRol =
                        rol.equalsIgnoreCase(rolUsuario);
            }

            if (coincideBusqueda
                    && coincideEstado
                    && coincideRol) {

                usuarios.add(usuario);
            }
        }

        // =====================================================
        // DATOS PARA LA VISTA
        // =====================================================

        model.addAttribute(
                "usuarios",
                usuarios
        );

        model.addAttribute(
                "roles",
                roles
        );

        model.addAttribute(
                "buscar",
                buscar == null ? "" : buscar
        );

        model.addAttribute(
                "estadoSeleccionado",
                estado == null ? "" : estado
        );

        model.addAttribute(
                "rolSeleccionado",
                rol == null ? "" : rol
        );

        // =====================================================
        // ESTADÍSTICAS
        // =====================================================

        long totalUsuarios =
                todosLosUsuarios.size();

        long totalActivos =
                todosLosUsuarios.stream()
                        .filter(usuario ->
                                usuario.getEstado()
                                        == EstadoPersona.activo)
                        .count();

        long totalBloqueados =
                todosLosUsuarios.stream()
                        .filter(usuario ->
                                usuario.getEstado()
                                        == EstadoPersona.bloqueado)
                        .count();

        long totalSuspendidos =
                todosLosUsuarios.stream()
                        .filter(usuario ->
                                usuario.getEstado()
                                        == EstadoPersona.suspendido)
                        .count();

        model.addAttribute(
                "totalUsuarios",
                totalUsuarios
        );

        model.addAttribute(
                "totalActivos",
                totalActivos
        );

        model.addAttribute(
                "totalBloqueados",
                totalBloqueados
        );

        model.addAttribute(
                "totalSuspendidos",
                totalSuspendidos
        );

        return "admin/usuarios/lista";
    }

    // =========================================================
    // VER DETALLE
    // =========================================================

    @GetMapping("/{id}")
    public String verUsuario(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Persona usuario =
                personaRepository.findById(id)
                        .orElse(null);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        String rol =
                obtenerRol(usuario);

        String tipoDocumento =
                "No registrado";

        if (usuario.getTipoDocumentoId() != null
                && !usuario.getTipoDocumentoId().isBlank()) {

            tipoDocumento =
                    tipoDocumentoRepository
                            .findById(
                                    usuario.getTipoDocumentoId()
                            )
                            .map(
                                    TipoDocumento::getNDoc
                            )
                            .orElse(
                                    usuario.getTipoDocumentoId()
                            );
        }

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "rol",
                rol
        );

        model.addAttribute(
                "nombreTipoDocumento",
                tipoDocumento
        );

        return "admin/usuarios/detalle";
    }

    // =========================================================
    // FORMULARIO EDITAR
    // =========================================================

    @GetMapping("/{id}/editar")
    public String editarFormulario(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Persona usuario =
                personaRepository.findById(id)
                        .orElse(null);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró el usuario seleccionado."
            );

            return "redirect:/admin/usuarios";
        }

        List<TipoDocumento> tiposDocumento =
                tipoDocumentoRepository.findAll();

        model.addAttribute(
                "usuario",
                usuario
        );

        model.addAttribute(
                "tiposDocumento",
                tiposDocumento
        );

        model.addAttribute(
                "rol",
                obtenerRol(usuario)
        );

        return "admin/usuarios/editar";
    }

    // =========================================================
    // GUARDAR CAMBIOS DEL USUARIO
    // =========================================================

    @PostMapping("/{id}/editar")
    public String guardarEdicion(
            @PathVariable Integer id,

            @RequestParam String tipoDocumentoId,

            @RequestParam String numeroDocumento,

            @RequestParam String primerNombre,

            @RequestParam(required = false)
            String segundoNombre,

            @RequestParam String primerApellido,

            @RequestParam(required = false)
            String segundoApellido,

            @RequestParam String email,

            @RequestParam String numeroTel,

            @RequestParam(required = false)
            Integer estrato,

            RedirectAttributes redirectAttributes) {

        Persona usuario =
                personaRepository.findById(id)
                        .orElse(null);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        // =====================================================
        // LIMPIAR INFORMACIÓN
        // =====================================================

        tipoDocumentoId =
                tipoDocumentoId == null
                        ? ""
                        : tipoDocumentoId.trim();

        numeroDocumento =
                numeroDocumento == null
                        ? ""
                        : numeroDocumento.trim();

        primerNombre =
                primerNombre == null
                        ? ""
                        : primerNombre.trim();

        primerApellido =
                primerApellido == null
                        ? ""
                        : primerApellido.trim();

        email =
                email == null
                        ? ""
                        : email.trim().toLowerCase();

        numeroTel =
                numeroTel == null
                        ? ""
                        : numeroTel.trim();

        if (segundoNombre != null) {
            segundoNombre =
                    segundoNombre.trim();
        }

        if (segundoApellido != null) {
            segundoApellido =
                    segundoApellido.trim();
        }

        // =====================================================
        // VALIDACIONES
        // =====================================================

        if (tipoDocumentoId.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debe seleccionar un tipo de documento."
            );

            return "redirect:/admin/usuarios/"
                    + id
                    + "/editar";
        }

        if (numeroDocumento.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El número de documento es obligatorio."
            );

            return "redirect:/admin/usuarios/"
                    + id
                    + "/editar";
        }

        if (primerNombre.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El primer nombre es obligatorio."
            );

            return "redirect:/admin/usuarios/"
                    + id
                    + "/editar";
        }

        if (primerApellido.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El primer apellido es obligatorio."
            );

            return "redirect:/admin/usuarios/"
                    + id
                    + "/editar";
        }

        if (email.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El correo electrónico es obligatorio."
            );

            return "redirect:/admin/usuarios/"
                    + id
                    + "/editar";
        }

        if (numeroTel.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El teléfono es obligatorio."
            );

            return "redirect:/admin/usuarios/"
                    + id
                    + "/editar";
        }

        // =====================================================
        // DOCUMENTO DUPLICADO
        // =====================================================

        boolean documentoDuplicado =
                personaRepository
                        .existsByNumeroDocumento(
                                numeroDocumento
                        )
                && !numeroDocumento.equals(
                        usuario.getNumeroDocumento()
                );

        if (documentoDuplicado) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El número de documento ya pertenece a otro usuario."
            );

            return "redirect:/admin/usuarios/"
                    + id
                    + "/editar";
        }

        // =====================================================
        // CORREO DUPLICADO
        // =====================================================

        boolean correoDuplicado =
                personaRepository
                        .existsByEmailIgnoreCase(
                                email
                        )
                && (
                    usuario.getEmail() == null
                    || !email.equalsIgnoreCase(
                            usuario.getEmail()
                    )
                );

        if (correoDuplicado) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El correo electrónico ya pertenece a otro usuario."
            );

            return "redirect:/admin/usuarios/"
                    + id
                    + "/editar";
        }

        // =====================================================
        // TIPO DOCUMENTO
        // =====================================================

        boolean existeTipoDocumento =
                tipoDocumentoRepository
                        .existsById(tipoDocumentoId);

        if (!existeTipoDocumento) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El tipo de documento seleccionado no existe."
            );

            return "redirect:/admin/usuarios/"
                    + id
                    + "/editar";
        }

        // =====================================================
        // ACTUALIZAR
        // =====================================================

        usuario.setTipoDocumentoId(
                tipoDocumentoId
        );

        usuario.setNumeroDocumento(
                numeroDocumento
        );

        usuario.setPrimerNombre(
                primerNombre
        );

        usuario.setSegundoNombre(
                segundoNombre == null
                        || segundoNombre.isBlank()
                        ? null
                        : segundoNombre
        );

        usuario.setPrimerApellido(
                primerApellido
        );

        usuario.setSegundoApellido(
                segundoApellido == null
                        || segundoApellido.isBlank()
                        ? null
                        : segundoApellido
        );

        usuario.setEmail(
                email
        );

        usuario.setNumeroTel(
                numeroTel
        );

        /*
         * NO guardamos estrato porque tu entidad Persona
         * actualmente lo tiene como @Transient y la tabla
         * personas no tiene una columna para ese dato.
         */

        personaRepository.save(usuario);

        redirectAttributes.addFlashAttribute(
                "success",
                "Usuario actualizado correctamente."
        );

        return "redirect:/admin/usuarios/" + id;
    }

    // =========================================================
    // CAMBIAR ESTADO
    // =========================================================

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String nuevoEstado,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Persona usuario =
                personaRepository.findById(id)
                        .orElse(null);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        EstadoPersona estado;

        try {

            estado =
                    EstadoPersona.valueOf(
                            nuevoEstado.toLowerCase()
                    );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El estado seleccionado no es válido."
            );

            return "redirect:/admin/usuarios/"
                    + id;
        }

        // =====================================================
        // NO BLOQUEARSE A SÍ MISMO
        // =====================================================

        if (authentication != null
                && authentication.getName() != null
                && usuario.getEmail() != null
                && usuario.getEmail()
                        .equalsIgnoreCase(
                                authentication.getName()
                        )
                && (
                    estado == EstadoPersona.bloqueado
                    || estado == EstadoPersona.suspendido
                )) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No puedes bloquear o suspender tu propia cuenta."
            );

            return "redirect:/admin/usuarios/"
                    + id;
        }

        usuario.setEstado(estado);

        personaRepository.save(usuario);

        redirectAttributes.addFlashAttribute(
                "success",
                "Estado del usuario actualizado correctamente."
        );

        return "redirect:/admin/usuarios/"
                + id;
    }

    // =========================================================
    // CAMBIAR CONTRASEÑA
    // =========================================================

    @PostMapping("/{id}/password")
    public String cambiarPassword(
            @PathVariable Integer id,
            @RequestParam String nuevaPassword,
            @RequestParam String confirmarPassword,
            RedirectAttributes redirectAttributes) {

        Persona usuario =
                personaRepository.findById(id)
                        .orElse(null);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/admin/usuarios";
        }

        if (nuevaPassword == null
                || nuevaPassword.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La nueva contraseña es obligatoria."
            );

            return "redirect:/admin/usuarios/"
                    + id;
        }

        if (nuevaPassword.length() < 6) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La contraseña debe tener mínimo 6 caracteres."
            );

            return "redirect:/admin/usuarios/"
                    + id;
        }

        if (confirmarPassword == null
                || !nuevaPassword.equals(
                        confirmarPassword
                )) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Las contraseñas no coinciden."
            );

            return "redirect:/admin/usuarios/"
                    + id;
        }

        usuario.setPassword(
                passwordEncoder.encode(
                        nuevaPassword
                )
        );

        personaRepository.save(usuario);

        redirectAttributes.addFlashAttribute(
                "success",
                "Contraseña actualizada correctamente."
        );

        return "redirect:/admin/usuarios/"
                + id;
    }

    // =========================================================
    // ROLES
    // =========================================================

    private Map<Integer, String> construirMapaRoles() {

        Map<Integer, String> roles =
                new HashMap<>();

        List<Administrador> administradores =
                administradorRepository.findAll();

        for (Administrador administrador
                : administradores) {

            if (administrador.getPersona() != null
                    && administrador.getPersona()
                            .getIdPersona() != null) {

                roles.put(
                        administrador.getPersona()
                                .getIdPersona(),
                        "Administrador"
                );
            }
        }

        List<Cliente> clientes =
                clienteRepository.findAll();

        for (Cliente cliente : clientes) {

            if (cliente.getPersona() != null
                    && cliente.getPersona()
                            .getIdPersona() != null) {

                Integer id =
                        cliente.getPersona()
                                .getIdPersona();

                if (!roles.containsKey(id)) {

                    roles.put(
                            id,
                            "Cliente"
                    );
                }
            }
        }

        return roles;
    }

    private String obtenerRol(
            Persona usuario) {

        if (usuario == null
                || usuario.getIdPersona() == null) {

            return "Sin rol";
        }

        Integer id =
                usuario.getIdPersona();

        if (administradorRepository
                .existsByPersona_IdPersona(id)) {

            return "Administrador";
        }

        if (clienteRepository
                .existsByPersona_IdPersona(id)) {

            return "Cliente";
        }

        return "Sin rol";
    }

    // =========================================================
    // NOMBRE COMPLETO
    // =========================================================

    private String construirNombreCompleto(
            Persona usuario) {

        if (usuario == null) {
            return "";
        }

        StringBuilder nombre =
                new StringBuilder();

        agregarNombre(
                nombre,
                usuario.getPrimerNombre()
        );

        agregarNombre(
                nombre,
                usuario.getSegundoNombre()
        );

        agregarNombre(
                nombre,
                usuario.getPrimerApellido()
        );

        agregarNombre(
                nombre,
                usuario.getSegundoApellido()
        );

        return nombre.toString().trim();
    }

    private void agregarNombre(
            StringBuilder nombre,
            String parte) {

        if (parte != null
                && !parte.trim().isEmpty()) {

            if (nombre.length() > 0) {
                nombre.append(" ");
            }

            nombre.append(
                    parte.trim()
            );
        }
    }
}