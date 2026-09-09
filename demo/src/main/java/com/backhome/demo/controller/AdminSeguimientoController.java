package com.backhome.demo.controller;

import com.backhome.demo.model.Administrador;
import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.GestionSeguimiento;
import com.backhome.demo.model.Persona;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.repository.AdministradorRepository;
import com.backhome.demo.repository.GestionSeguimientoRepository;
import com.backhome.demo.repository.PersonaRepository;
import com.backhome.demo.repository.SeguimientoEncontradoRepository;
import com.backhome.demo.repository.SeguimientoPerdidoRepository;
import com.backhome.demo.repository.SeguimientoRepository;
import com.backhome.demo.repository.ImagenSeguimientoRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin/seguimientos")
public class AdminSeguimientoController {

    private final SeguimientoRepository seguimientoRepository;
    private final SeguimientoPerdidoRepository seguimientoPerdidoRepository;
    private final SeguimientoEncontradoRepository seguimientoEncontradoRepository;
    private final PersonaRepository personaRepository;
    private final AdministradorRepository administradorRepository;
    private final GestionSeguimientoRepository gestionSeguimientoRepository;
    private final ImagenSeguimientoRepository imagenSeguimientoRepository;

    public AdminSeguimientoController(
            SeguimientoRepository seguimientoRepository,
            SeguimientoPerdidoRepository seguimientoPerdidoRepository,
            SeguimientoEncontradoRepository seguimientoEncontradoRepository,
            PersonaRepository personaRepository,
            AdministradorRepository administradorRepository,
            GestionSeguimientoRepository gestionSeguimientoRepository,
            ImagenSeguimientoRepository imagenSeguimientoRepository) {

        this.seguimientoRepository = seguimientoRepository;
        this.seguimientoPerdidoRepository = seguimientoPerdidoRepository;
        this.seguimientoEncontradoRepository = seguimientoEncontradoRepository;
        this.personaRepository = personaRepository;
        this.administradorRepository = administradorRepository;
        this.gestionSeguimientoRepository = gestionSeguimientoRepository;
        this.imagenSeguimientoRepository = imagenSeguimientoRepository;
    }

    @GetMapping
    public String listarSeguimientos(Model model) {

        List<Seguimiento> pendientes =
                seguimientoRepository
                        .findByEstadoModeracionOrderByIdSeguimientoDesc(
                                EstadoModeracion.pendiente
                        );

        model.addAttribute(
                "seguimientos",
                pendientes
        );

        return "admin/seguimientos";
    }

    @GetMapping("/{id}")
    public String detalleSeguimiento(
            @PathVariable Integer id,
            Model model) {

        Seguimiento seguimiento =
                seguimientoRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Seguimiento no encontrado"
                                )
                        );

        model.addAttribute(
                "seguimiento",
                seguimiento
        );

        List<com.backhome.demo.model.ImagenSeguimiento> imagenes =
        imagenSeguimientoRepository
                .findBySeguimiento_IdSeguimiento(id);

model.addAttribute("imagenes", imagenes);


        if (seguimiento.getTipoSeguimiento()
                == com.backhome.demo.model.TipoSeguimiento.perdido) {

            seguimientoPerdidoRepository
                    .findBySeguimiento_IdSeguimiento(id)
                    .ifPresent(
                            detalle -> model.addAttribute(
                                    "seguimientoPerdido",
                                    detalle
                            )
                    );

        } else {

            seguimientoEncontradoRepository
                    .findBySeguimiento_IdSeguimiento(id)
                    .ifPresent(
                            detalle -> model.addAttribute(
                                    "seguimientoEncontrado",
                                    detalle
                            )
                    );
        }

        return "admin/seguimiento-detalle";
    }

    @PostMapping("/{id}/verificar")
    @Transactional
    public String verificarSeguimiento(
            @PathVariable Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Seguimiento seguimiento =
                seguimientoRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Seguimiento no encontrado"
                                )
                        );

        if (seguimiento.getEstadoModeracion()
                != EstadoModeracion.pendiente) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Este seguimiento ya fue moderado."
            );

            return "redirect:/admin/seguimientos";
        }

        Administrador administrador =
                obtenerAdministradorActual(authentication);

        seguimiento.setEstadoModeracion(
                EstadoModeracion.verificado
        );

        seguimientoRepository.save(seguimiento);

        GestionSeguimiento gestion =
                new GestionSeguimiento();

        gestion.setSeguimiento(seguimiento);
        gestion.setFechaSeguimiento(LocalDateTime.now());
        gestion.setAccion("VERIFICAR");
        gestion.setObservacion(
                "El reporte fue verificado por el administrador."
        );
        gestion.setAdministrador(administrador);

        gestionSeguimientoRepository.save(gestion);

        redirectAttributes.addFlashAttribute(
                "success",
                "El reporte fue verificado correctamente."
        );

        return "redirect:/admin/seguimientos";
    }

    @PostMapping("/{id}/rechazar")
    @Transactional
    public String rechazarSeguimiento(
            @PathVariable Integer id,
            @RequestParam("observacion") String observacion,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Seguimiento seguimiento =
                seguimientoRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Seguimiento no encontrado"
                                )
                        );

        if (seguimiento.getEstadoModeracion()
                != EstadoModeracion.pendiente) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Este seguimiento ya fue moderado."
            );

            return "redirect:/admin/seguimientos";
        }

        if (observacion == null || observacion.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debes indicar una observación para rechazar el reporte."
            );

            return "redirect:/admin/seguimientos/" + id;
        }

        Administrador administrador =
                obtenerAdministradorActual(authentication);

        seguimiento.setEstadoModeracion(
                EstadoModeracion.rechazado
        );

        seguimientoRepository.save(seguimiento);

        GestionSeguimiento gestion =
                new GestionSeguimiento();

        gestion.setSeguimiento(seguimiento);
        gestion.setFechaSeguimiento(LocalDateTime.now());
        gestion.setAccion("RECHAZAR");
        gestion.setObservacion(observacion.trim());
        gestion.setAdministrador(administrador);

        gestionSeguimientoRepository.save(gestion);

        redirectAttributes.addFlashAttribute(
                "success",
                "El reporte fue rechazado correctamente."
        );

        return "redirect:/admin/seguimientos";
    }

    private Administrador obtenerAdministradorActual(
            Authentication authentication) {

        String email = authentication.getName();

        Persona persona =
                personaRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró la persona asociada al usuario."
                                )
                        );

        return administradorRepository
                .findByPersona_IdPersona(persona.getIdPersona())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario autenticado no es un administrador."
                        )
                );
    }
}