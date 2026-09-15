package com.backhome.demo.controller;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.backhome.demo.dto.EstadisticaLocalidad;
import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.Localidad;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.model.TipoSeguimiento;
import com.backhome.demo.repository.LocalidadRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Controller
public class MapaController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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

        // =========================================================
        // 1. OBTENER LOCALIDADES
        // =========================================================

        List<Localidad> localidades =
                localidadRepository.findAllByOrderByNombreAsc();


        // =========================================================
        // 2. OBTENER SOLO SEGUIMIENTOS VERIFICADOS
        // =========================================================

        List<Seguimiento> seguimientos =
                seguimientoRepository.buscarSeguimientosPublicos(
                        EstadoModeracion.verificado,
                        null,
                        null,
                        null,
                        null,
                        null
                );


        // =========================================================
        // 3. CONVERTIR SEGUIMIENTOS PARA EL MAPA
        // =========================================================
        // No enviamos directamente las entidades JPA al JavaScript.
        // Usamos el DTO que ya tenías para evitar problemas de
        // serialización.

        List<SeguimientoMapaDTO> seguimientosMapa =
                seguimientos.stream()
                        .map(this::convertirParaMapa)
                        .toList();


        // =========================================================
        // 4. CREAR ESTADÍSTICAS PARA TODAS LAS LOCALIDADES
        // =========================================================

        Map<Integer, EstadisticaLocalidad> estadisticas =
                new LinkedHashMap<>();

        for (Localidad localidad : localidades) {

            EstadisticaLocalidad estadistica =
                    new EstadisticaLocalidad(
                            localidad.getIdLocalidad(),
                            localidad.getNombre(),
                            localidad.getPoblacion()
                    );

            estadisticas.put(
                    localidad.getIdLocalidad(),
                    estadistica
            );
        }


        // =========================================================
        // 5. CONTAR REPORTES POR LOCALIDAD
        // =========================================================

        for (Seguimiento seguimiento : seguimientos) {

            // Si el seguimiento no tiene lugar o localidad,
            // no podemos asociarlo a una localidad.
            if (seguimiento.getLugar() == null
                    || seguimiento.getLugar().getLocalidad() == null) {

                continue;
            }

            Integer idLocalidad =
                    seguimiento.getLugar()
                            .getLocalidad()
                            .getIdLocalidad();

            EstadisticaLocalidad estadistica =
                    estadisticas.get(idLocalidad);

            if (estadistica == null) {
                continue;
            }


            // Contar según el tipo de seguimiento

            if (seguimiento.getTipoSeguimiento()
                    == TipoSeguimiento.perdido) {

                estadistica.agregarPerdido();

            } else if (seguimiento.getTipoSeguimiento()
                    == TipoSeguimiento.encontrado) {

                estadistica.agregarEncontrado();
            }
        }


        // =========================================================
        // 6. CREAR RANKING POR TASA DE REPORTES
        // =========================================================

        List<EstadisticaLocalidad> ranking =
                estadisticas.values()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        EstadisticaLocalidad::getTasaReportes
                                ).reversed()
                        )
                        .collect(Collectors.toList());


        // =========================================================
        // 7. ENVIAR INFORMACIÓN A THYMELEAF
        // =========================================================

        model.addAttribute(
                "localidades",
                localidades
        );

        model.addAttribute(
                "seguimientos",
                seguimientosMapa
        );

        model.addAttribute(
                "estadisticasLocalidades",
                estadisticas.values()
        );

        model.addAttribute(
                "rankingLocalidades",
                ranking
        );


        return "mapa";
    }


    // =============================================================
    // CONVERTIR SEGUIMIENTO A DTO PARA EL MAPA
    // =============================================================

    private SeguimientoMapaDTO convertirParaMapa(
            Seguimiento seguimiento) {

        String localidadNombre = null;

        if (seguimiento.getLugar() != null
                && seguimiento.getLugar().getLocalidad() != null) {

            localidadNombre =
                    seguimiento.getLugar()
                            .getLocalidad()
                            .getNombre();
        }


        String fecha = null;

        if (seguimiento.getFechaPublicacion() != null) {

            fecha =
                    seguimiento.getFechaPublicacion()
                            .format(FORMATO_FECHA);
        }


        return new SeguimientoMapaDTO(
                seguimiento.getIdSeguimiento(),
                seguimiento.getTitulo(),

                seguimiento.getTipoSeguimiento() != null
                        ? seguimiento.getTipoSeguimiento().name()
                        : null,

                seguimiento.getEstadoSeguimiento() != null
                        ? seguimiento.getEstadoSeguimiento().name()
                        : null,

                localidadNombre,
                fecha
        );
    }


    // =============================================================
    // DTO UTILIZADO POR EL MAPA
    // =============================================================

    public record SeguimientoMapaDTO(
            Integer idSeguimiento,
            String titulo,
            String tipoSeguimiento,
            String estadoSeguimiento,
            String localidadNombre,
            String fechaPublicacion
    ) {
    }
}