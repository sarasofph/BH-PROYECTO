package com.backhome.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.Localidad;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.repository.LocalidadRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Controller
public class MapaController {

    private final LocalidadRepository localidadRepository;
    private final SeguimientoRepository seguimientoRepository;

    public MapaController(
            LocalidadRepository localidadRepository,
            SeguimientoRepository seguimientoRepository) {

        this.localidadRepository = localidadRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    @GetMapping("/mapa")
    public String mostrarMapa(Model model) {

        // Localidades de Bogotá
        List<Localidad> localidades =
                localidadRepository.findAllByOrderByNombreAsc();

        // Solo seguimientos verificados por el administrador
        List<Seguimiento> seguimientos =
                seguimientoRepository.buscarSeguimientosPublicos(
                        EstadoModeracion.verificado,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        model.addAttribute("localidades", localidades);
        model.addAttribute("seguimientos", seguimientos);

        return "mapa";
    }
}