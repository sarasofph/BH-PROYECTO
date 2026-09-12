package com.backhome.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.ImagenSeguimiento;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.model.TipoSeguimiento;
import com.backhome.demo.repository.ImagenSeguimientoRepository;
import com.backhome.demo.repository.SeguimientoEncontradoRepository;
import com.backhome.demo.repository.SeguimientoPerdidoRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Controller
@RequestMapping("/seguimientos")
public class SeguimientoPublicoController {

    private final SeguimientoRepository seguimientoRepository;
    private final SeguimientoPerdidoRepository seguimientoPerdidoRepository;
    private final SeguimientoEncontradoRepository seguimientoEncontradoRepository;
    private final ImagenSeguimientoRepository imagenSeguimientoRepository;

    public SeguimientoPublicoController(
            SeguimientoRepository seguimientoRepository,
            SeguimientoPerdidoRepository seguimientoPerdidoRepository,
            SeguimientoEncontradoRepository seguimientoEncontradoRepository,
            ImagenSeguimientoRepository imagenSeguimientoRepository) {

        this.seguimientoRepository = seguimientoRepository;
        this.seguimientoPerdidoRepository = seguimientoPerdidoRepository;
        this.seguimientoEncontradoRepository = seguimientoEncontradoRepository;
        this.imagenSeguimientoRepository = imagenSeguimientoRepository;
    }

    @GetMapping
    public String listar(Model model) {

        List<Seguimiento> seguimientos =
                seguimientoRepository
                        .findByEstadoModeracionOrderByIdSeguimientoDesc(
                                EstadoModeracion.verificado
                        );

        Map<Integer, ImagenSeguimiento> imagenesPrincipales =
                new HashMap<>();

        for (Seguimiento seguimiento : seguimientos) {

            List<ImagenSeguimiento> imagenes =
                    imagenSeguimientoRepository
                            .findBySeguimiento_IdSeguimientoAndImagenPrincipalTrue(
                                    seguimiento.getIdSeguimiento()
                            );

            if (!imagenes.isEmpty()) {

                imagenesPrincipales.put(
                        seguimiento.getIdSeguimiento(),
                        imagenes.get(0)
                );
            }
        }

        model.addAttribute("seguimientos", seguimientos);
        model.addAttribute(
                "imagenesPrincipales",
                imagenesPrincipales
        );

        return "seguimientos";
    }

    @GetMapping("/{id}")
    public String detalle(
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

        /*
         * Solo los seguimientos verificados
         * pueden verse públicamente.
         */
        if (seguimiento.getEstadoModeracion()
                != EstadoModeracion.verificado) {

            return "redirect:/seguimientos";
        }

        model.addAttribute(
                "seguimiento",
                seguimiento
        );

        List<ImagenSeguimiento> imagenes =
                imagenSeguimientoRepository
                        .findBySeguimiento_IdSeguimiento(id);

        model.addAttribute(
                "imagenes",
                imagenes
        );

        if (seguimiento.getTipoSeguimiento()
                == TipoSeguimiento.perdido) {

            seguimientoPerdidoRepository
                    .findBySeguimiento_IdSeguimiento(id)
                    .ifPresent(detalle ->
                            model.addAttribute(
                                    "seguimientoPerdido",
                                    detalle
                            )
                    );

        } else {

            seguimientoEncontradoRepository
                    .findBySeguimiento_IdSeguimiento(id)
                    .ifPresent(detalle ->
                            model.addAttribute(
                                    "seguimientoEncontrado",
                                    detalle
                            )
                    );
        }

        return "seguimiento-detalle";
    }
}