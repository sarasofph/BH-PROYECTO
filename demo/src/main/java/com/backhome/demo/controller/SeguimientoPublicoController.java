package com.backhome.demo.controller;

import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.model.TipoSeguimiento;
import com.backhome.demo.repository.SeguimientoEncontradoRepository;
import com.backhome.demo.repository.SeguimientoPerdidoRepository;
import com.backhome.demo.repository.SeguimientoRepository;

import java.util.List;

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

    public SeguimientoPublicoController(
            SeguimientoRepository seguimientoRepository,
            SeguimientoPerdidoRepository seguimientoPerdidoRepository,
            SeguimientoEncontradoRepository seguimientoEncontradoRepository) {

        this.seguimientoRepository = seguimientoRepository;
        this.seguimientoPerdidoRepository = seguimientoPerdidoRepository;
        this.seguimientoEncontradoRepository = seguimientoEncontradoRepository;
    }

    @GetMapping
    public String listar(Model model) {

        List<Seguimiento> seguimientos =
                seguimientoRepository
                        .findByEstadoModeracionOrderByIdSeguimientoDesc(
                                EstadoModeracion.verificado
                        );

        model.addAttribute("seguimientos", seguimientos);

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