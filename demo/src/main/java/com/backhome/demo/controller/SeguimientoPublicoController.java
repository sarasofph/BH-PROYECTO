package com.backhome.demo.controller;

import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.model.TipoSeguimiento;
import com.backhome.demo.repository.SeguimientoEncontradoRepository;
import com.backhome.demo.repository.SeguimientoPerdidoRepository;
import com.backhome.demo.repository.SeguimientoRepository;
import com.backhome.demo.model.ImagenSeguimiento;
import com.backhome.demo.repository.ImagenSeguimientoRepository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

        Map<Integer, ImagenSeguimiento> imagenesPrincipales = new HashMap<>();

for (Seguimiento seguimiento : seguimientos) {

    imagenSeguimientoRepository
            .findBySeguimiento_IdSeguimientoAndImagenPrincipalTrue(
                    seguimiento.getIdSeguimiento()
            )
            .ifPresent(imagen ->
                    imagenesPrincipales.put(
                            seguimiento.getIdSeguimiento(),
                            imagen
                    )
            );
}

model.addAttribute("seguimientos", seguimientos);
model.addAttribute("imagenesPrincipales", imagenesPrincipales);

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

        // Nunca permitir que un seguimiento no verificado
        // sea visible públicamente.
        if (seguimiento.getEstadoModeracion()
                != EstadoModeracion.verificado) {

            return "redirect:/seguimientos";
        }

        model.addAttribute("seguimiento", seguimiento);

        List<com.backhome.demo.model.ImagenSeguimiento> imagenes =
        imagenSeguimientoRepository
                .findBySeguimiento_IdSeguimiento(id);

model.addAttribute("imagenes", imagenes);

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