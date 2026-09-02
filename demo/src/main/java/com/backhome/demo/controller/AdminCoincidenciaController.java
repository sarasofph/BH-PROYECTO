package com.backhome.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.Coincidencia;
import com.backhome.demo.model.Coincidencia.Estado;
import com.backhome.demo.repository.CoincidenciaRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Controller
@RequestMapping("/admin/coincidencias")
public class AdminCoincidenciaController {

    private final CoincidenciaRepository coincidenciaRepository;
    private final SeguimientoRepository seguimientoRepository;

    public AdminCoincidenciaController(
            CoincidenciaRepository coincidenciaRepository,
            SeguimientoRepository seguimientoRepository) {

        this.coincidenciaRepository = coincidenciaRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String estado,
            Model model) {

        if (estado != null && !estado.isBlank()) {

            try {

                model.addAttribute(
                        "coincidencias",
                        coincidenciaRepository
                                .findByEstadoOrderByIdCoincidenciaDesc(
                                        Estado.valueOf(estado)
                                )
                );

            } catch (IllegalArgumentException e) {

                model.addAttribute(
                        "coincidencias",
                        coincidenciaRepository
                                .findAllByOrderByIdCoincidenciaDesc()
                );
            }

        } else {

            model.addAttribute(
                    "coincidencias",
                    coincidenciaRepository
                            .findAllByOrderByIdCoincidenciaDesc()
            );
        }

        model.addAttribute(
                "estados",
                Estado.values()
        );

        model.addAttribute(
                "estado",
                estado == null ? "" : estado
        );

        return "admin/coincidencias/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {

        model.addAttribute(
                "coincidencia",
                new Coincidencia()
        );

        model.addAttribute(
                "seguimientos",
                seguimientoRepository
                        .findAllByOrderByIdSeguimientoDesc()
        );

        return "admin/coincidencias/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Coincidencia coincidencia,
            @RequestParam Integer perdidoId,
            @RequestParam Integer encontradoId,
            RedirectAttributes redirectAttributes) {

        if (perdidoId.equals(encontradoId)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El seguimiento perdido y encontrado no pueden ser iguales."
            );

            return "redirect:/admin/coincidencias/nueva";
        }

        if (coincidenciaRepository
                .existsBySeguimientoPerdido_IdSeguimientoAndSeguimientoEncontrado_IdSeguimiento(
                        perdidoId,
                        encontradoId
                )) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Esta coincidencia ya existe."
            );

            return "redirect:/admin/coincidencias/nueva";
        }

        coincidencia.setSeguimientoPerdido(
                seguimientoRepository.findById(perdidoId)
                        .orElseThrow()
        );

        coincidencia.setSeguimientoEncontrado(
                seguimientoRepository.findById(encontradoId)
                        .orElseThrow()
        );

        if (coincidencia.getEstado() == null) {
            coincidencia.setEstado(Estado.pendiente);
        }

        coincidenciaRepository.save(coincidencia);

        redirectAttributes.addFlashAttribute(
                "success",
                "Coincidencia registrada correctamente."
        );

        return "redirect:/admin/coincidencias";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes) {

        Coincidencia coincidencia =
                coincidenciaRepository.findById(id)
                        .orElseThrow();

        try {

            coincidencia.setEstado(
                    Estado.valueOf(estado)
            );

            coincidenciaRepository.save(coincidencia);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Estado de coincidencia actualizado."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Estado inválido."
            );
        }

        return "redirect:/admin/coincidencias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        if (coincidenciaRepository.existsById(id)) {

            coincidenciaRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Coincidencia eliminada correctamente."
            );
        }

        return "redirect:/admin/coincidencias";
    }
}