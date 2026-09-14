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
import com.backhome.demo.model.ActualizacionSeguimiento;
import com.backhome.demo.model.HistorialEstadoSeguimiento;
import com.backhome.demo.model.HistorialEstadoCustodia;
import com.backhome.demo.repository.HistorialEstadoSeguimientoRepository;
import com.backhome.demo.repository.HistorialEstadoCustodiaRepository;


import com.backhome.demo.repository.ImagenSeguimientoRepository;
import com.backhome.demo.repository.SeguimientoEncontradoRepository;
import com.backhome.demo.repository.SeguimientoPerdidoRepository;
import com.backhome.demo.repository.SeguimientoRepository;
import com.backhome.demo.repository.ActualizacionSeguimientoRepository;


@Controller
@RequestMapping("/seguimientos")
public class SeguimientoPublicoController {

    private final SeguimientoRepository seguimientoRepository;
    private final SeguimientoPerdidoRepository seguimientoPerdidoRepository;
    private final SeguimientoEncontradoRepository seguimientoEncontradoRepository;
    private final ImagenSeguimientoRepository imagenSeguimientoRepository;
    private final ActualizacionSeguimientoRepository actualizacionSeguimientoRepository;
    private final HistorialEstadoSeguimientoRepository historialEstadoSeguimientoRepository;
private final HistorialEstadoCustodiaRepository historialEstadoCustodiaRepository;

    public SeguimientoPublicoController(
        SeguimientoRepository seguimientoRepository,
        SeguimientoPerdidoRepository seguimientoPerdidoRepository,
        SeguimientoEncontradoRepository seguimientoEncontradoRepository,
        ImagenSeguimientoRepository imagenSeguimientoRepository,
        ActualizacionSeguimientoRepository actualizacionSeguimientoRepository,
        HistorialEstadoSeguimientoRepository historialEstadoSeguimientoRepository,
        HistorialEstadoCustodiaRepository historialEstadoCustodiaRepository) {

    this.seguimientoRepository = seguimientoRepository;
    this.seguimientoPerdidoRepository = seguimientoPerdidoRepository;
    this.seguimientoEncontradoRepository = seguimientoEncontradoRepository;
    this.imagenSeguimientoRepository = imagenSeguimientoRepository;
    this.actualizacionSeguimientoRepository = actualizacionSeguimientoRepository;
    this.historialEstadoSeguimientoRepository = historialEstadoSeguimientoRepository;
    this.historialEstadoCustodiaRepository = historialEstadoCustodiaRepository;
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

        model.addAttribute(
        "personaContacto",
        seguimiento.getCliente().getPersona()
);

        List<ImagenSeguimiento> imagenes =
                imagenSeguimientoRepository
                        .findBySeguimiento_IdSeguimiento(id);

        model.addAttribute(
                "imagenes",
                imagenes
        );

        List<ActualizacionSeguimiento> actualizaciones =
        actualizacionSeguimientoRepository
                .findBySeguimiento_IdSeguimientoOrderByCreatedAtDesc(id);

model.addAttribute(
        "actualizaciones",
        actualizaciones
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
            .ifPresent(detalle -> {

                model.addAttribute(
                        "seguimientoEncontrado",
                        detalle
                );

                List<HistorialEstadoCustodia> historialCustodia =
                        historialEstadoCustodiaRepository
                                .findBySeguimientoEncontrado_IdEncontradoOrderByFechaCambioDesc(
                                        detalle.getIdEncontrado()
                                );

                model.addAttribute(
                        "historialCustodia",
                        historialCustodia
                );
            });
}

        
    List<HistorialEstadoSeguimiento> historialEstado =
        historialEstadoSeguimientoRepository
                .findBySeguimiento_IdSeguimientoOrderByFechaCambioDesc(id);

model.addAttribute(
        "historialEstado",
        historialEstado
);

    return "seguimiento-detalle";
}
}