package com.backhome.demo.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.Persona;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;
import com.backhome.demo.repository.SeguimientoRepository;
import com.backhome.demo.service.FotoPerfilService;

@Controller
public class ClienteController {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final FotoPerfilService fotoPerfilService;
    private final PasswordEncoder passwordEncoder;

    public ClienteController(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            SeguimientoRepository seguimientoRepository,
            FotoPerfilService fotoPerfilService,
            PasswordEncoder passwordEncoder) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.fotoPerfilService = fotoPerfilService;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // PERFIL
    // =========================================================

    @GetMapping("/cliente/perfil")
    public String perfil(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        Persona persona = personaRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la persona."
                        )
                );

        Cliente cliente = clienteRepository
                .findByPersona_IdPersona(persona.getIdPersona())
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró el cliente asociado."
                        )
                );

        List<Seguimiento> seguimientos =
                seguimientoRepository
                        .findByCliente_IdClienteOrderByIdSeguimientoDesc(
                                cliente.getIdCliente()
                        );

        long enRevision = seguimientos.stream()
                .filter(s ->
                        s.getEstadoModeracion()
                                == EstadoModeracion.pendiente)
                .count();

        long rechazados = seguimientos.stream()
                .filter(s ->
                        s.getEstadoModeracion()
                                == EstadoModeracion.rechazado)
                .count();

        long verificados = seguimientos.stream()
                .filter(s ->
                        s.getEstadoModeracion()
                                == EstadoModeracion.verificado)
                .count();

        model.addAttribute("persona", persona);
        model.addAttribute("cliente", cliente);

        model.addAttribute(
                "totalSeguimientos",
                seguimientos.size()
        );

        model.addAttribute(
                "enRevision",
                enRevision
        );

        model.addAttribute(
                "rechazados",
                rechazados
        );

        model.addAttribute(
                "verificados",
                verificados
        );

        return "cliente/perfil";
    }

    // =========================================================
    // EDITAR PERFIL
    // =========================================================

    @GetMapping("/cliente/perfil/editar")
    public String editarPerfil(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        Persona persona = personaRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la persona."
                        )
                );

        model.addAttribute("persona", persona);

        return "cliente/editar-perfil";
    }

    @PostMapping("/cliente/perfil/editar")
    public String guardarPerfil(
            Authentication authentication,
            @RequestParam String primerNombre,
            @RequestParam(required = false) String segundoNombre,
            @RequestParam String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String numeroTel,
            @RequestParam("foto") MultipartFile foto) {

        String email = authentication.getName();

        Persona persona = personaRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la persona."
                        )
                );

        persona.setPrimerNombre(primerNombre);
        persona.setSegundoNombre(segundoNombre);
        persona.setPrimerApellido(primerApellido);
        persona.setSegundoApellido(segundoApellido);
        persona.setNumeroTel(numeroTel);

        // Si el usuario seleccionó una nueva foto,
        // se guarda y se elimina la anterior.
        if (foto != null && !foto.isEmpty()) {

            String rutaFoto =
                    fotoPerfilService.guardarFoto(
                            foto,
                            persona.getFotoPerfil()
                    );

            persona.setFotoPerfil(rutaFoto);
        }

        personaRepository.save(persona);

        return "redirect:/cliente/perfil";
    }

    // =========================================================
    // CAMBIAR CONTRASEÑA - MOSTRAR FORMULARIO
    // =========================================================

    @GetMapping("/cliente/perfil/cambiar-contrasena")
    public String cambiarContrasena() {

        return "cliente/cambiar-contrasena";
    }

    // =========================================================
    // CAMBIAR CONTRASEÑA - GUARDAR
    // =========================================================

    @PostMapping("/cliente/perfil/cambiar-contrasena")
    public String guardarNuevaContrasena(
            Authentication authentication,
            @RequestParam String contrasenaActual,
            @RequestParam String nuevaContrasena,
            @RequestParam String confirmarContrasena,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();

        Persona persona = personaRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la persona."
                        )
                );

        // 1. Verificar la contraseña actual
        if (!passwordEncoder.matches(
                contrasenaActual,
                persona.getPassword())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La contraseña actual no es correcta."
            );

            return "redirect:/cliente/perfil/cambiar-contrasena";
        }

        // 2. Verificar que las nuevas contraseñas coincidan
        if (!nuevaContrasena.equals(confirmarContrasena)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Las nuevas contraseñas no coinciden."
            );

            return "redirect:/cliente/perfil/cambiar-contrasena";
        }

        // 3. Evitar reutilizar exactamente la misma contraseña
        if (passwordEncoder.matches(
                nuevaContrasena,
                persona.getPassword())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La nueva contraseña debe ser diferente a la actual."
            );

            return "redirect:/cliente/perfil/cambiar-contrasena";
        }

        // 4. Validación básica de longitud
        if (nuevaContrasena.length() < 8) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La nueva contraseña debe tener mínimo 8 caracteres."
            );

            return "redirect:/cliente/perfil/cambiar-contrasena";
        }

        // 5. Encriptar la nueva contraseña con BCrypt
        String nuevaContrasenaEncriptada =
                passwordEncoder.encode(nuevaContrasena);

        persona.setPassword(nuevaContrasenaEncriptada);

        personaRepository.save(persona);

        redirectAttributes.addFlashAttribute(
                "exito",
                "Tu contraseña fue cambiada correctamente."
        );

        return "redirect:/cliente/perfil";
    }
}