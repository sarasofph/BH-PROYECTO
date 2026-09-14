package com.backhome.demo.controller;

import java.time.format.DateTimeFormatter;
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

        /*
         * IMPORTANTE: no se pasan las entidades Seguimiento
         * directamente al modelo para el mapa.
         *
         * Thymeleaf usa su PROPIO ObjectMapper (independiente
         * del que configura Spring Boot) para convertir a JSON
         * dentro de th:inline="javascript", y ese ObjectMapper
         * no soporta java.time.LocalDateTime por defecto. Eso
         * rompía la página a medio generar (InvalidDefinitionException
         * en el campo fechaPublicacion), lo que en el navegador
         * se veía como un ERR_INCOMPLETE_CHUNKED_ENCODING.
         *
         * Se arma una lista liviana (record) con solo los
         * campos que el mapa necesita y la fecha ya convertida
         * a texto, evitando ese problema por completo.
         */
        List<SeguimientoMapaDTO> seguimientosMapa =
                seguimientos.stream()
                        .map(this::convertirParaMapa)
                        .toList();

        model.addAttribute("localidades", localidades);
        model.addAttribute("seguimientos", seguimientosMapa);

        return "mapa";
    }

    private SeguimientoMapaDTO convertirParaMapa(Seguimiento seguimiento) {

        String localidadNombre = null;

        if (seguimiento.getLugar() != null
                && seguimiento.getLugar().getLocalidad() != null) {

            localidadNombre =
                    seguimiento.getLugar().getLocalidad().getNombre();
        }

        String fecha = null;

        if (seguimiento.getFechaPublicacion() != null) {
            fecha = seguimiento.getFechaPublicacion().format(FORMATO_FECHA);
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

    /**
     * Datos mínimos que necesita el mapa para dibujar cada
     * seguimiento. Al ser un record simple (sin relaciones JPA
     * ni tipos java.time), Thymeleaf lo convierte a JSON sin
     * problemas dentro de th:inline="javascript".
     */
    public record SeguimientoMapaDTO(
            Integer idSeguimiento,
            String titulo,
            String tipoSeguimiento,
            String estadoSeguimiento,
            String localidadNombre,
            String fechaPublicacion
    ) {}
}