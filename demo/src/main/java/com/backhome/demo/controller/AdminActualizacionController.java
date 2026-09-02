package com.backhome.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.ActualizacionSeguimiento;
import com.backhome.demo.repository.ActualizacionSeguimientoRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Controller
@RequestMapping("/admin/actualizaciones")
public class AdminActualizacionController {

    private final ActualizacionSeguimientoRepository actualizacionRepository;
    private final SeguimientoRepository seguimientoRepository;

    public AdminActualizacionController(
            ActualizacionSeguimientoRepository actualizacionRepository,
            SeguimientoRepository seguimientoRepository) {

        this.actualizacionRepository = actualizacionRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam Integer seguimientoId,
            @RequestParam String mensaje,
            RedirectAttributes redirectAttributes) {

        if (mensaje == null || mensaje.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El mensaje no puede estar vacío."
            );

            return "redirect:/admin/seguimientos/"
                    + seguimientoId;
        }

        ActualizacionSeguimiento actualizacion =
                new ActualizacionSeguimiento();

        actualizacion.setSeguimiento(
                seguimientoRepository.findById(seguimientoId)
                        .orElseThrow()
        );

        actualizacion.setMensaje(
                mensaje.trim()
        );

        actualizacionRepository.save(
                actualizacion
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Actualización agregada correctamente."
        );

        return "redirect:/admin/seguimientos/"
                + seguimientoId;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Integer id,
            @RequestParam Integer seguimientoId,
            RedirectAttributes redirectAttributes) {

        if (actualizacionRepository.existsById(id)) {

            actualizacionRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Actualización eliminada."
            );
        }

        return "redirect:/admin/seguimientos/"
                + seguimientoId;
    }
}
