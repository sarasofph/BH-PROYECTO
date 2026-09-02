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

import com.backhome.demo.model.Prioridad;
import com.backhome.demo.model.Prioridad.Estado;
import com.backhome.demo.repository.PrioridadRepository;

@Controller
@RequestMapping("/admin/prioridades")
public class AdminPrioridadController {

    private final PrioridadRepository prioridadRepository;

    public AdminPrioridadController(
            PrioridadRepository prioridadRepository) {

        this.prioridadRepository = prioridadRepository;
    }

    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
                "prioridades",
                prioridadRepository.findAllByOrderByNivelAsc()
        );

        return "admin/prioridades/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {

        Prioridad prioridad = new Prioridad();

        prioridad.setEstado(Estado.activo);

        model.addAttribute(
                "prioridad",
                prioridad
        );

        return "admin/prioridades/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Prioridad prioridad,
            RedirectAttributes redirectAttributes) {

        String nombre =
                prioridad.getNombre() == null
                        ? ""
                        : prioridad.getNombre().trim();

        if (nombre.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El nombre es obligatorio."
            );

            return "redirect:/admin/prioridades/nueva";
        }

        boolean nombreRepetido;

        if (prioridad.getIdPrioridad() == null) {

            nombreRepetido =
                    prioridadRepository
                            .existsByNombreIgnoreCase(nombre);

        } else {

            nombreRepetido =
                    prioridadRepository
                            .existsByNombreIgnoreCaseAndIdPrioridadNot(
                                    nombre,
                                    prioridad.getIdPrioridad()
                            );
        }

        if (nombreRepetido) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Ya existe una prioridad con ese nombre."
            );

            return "redirect:/admin/prioridades";
        }

        prioridad.setNombre(nombre);

        if (prioridad.getEstado() == null) {
            prioridad.setEstado(Estado.activo);
        }

        prioridadRepository.save(prioridad);

        redirectAttributes.addFlashAttribute(
                "success",
                "Prioridad guardada correctamente."
        );

        return "redirect:/admin/prioridades";
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Integer id,
            Model model) {

        model.addAttribute(
                "prioridad",
                prioridadRepository.findById(id)
                        .orElseThrow()
        );

        return "admin/prioridades/formulario";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes) {

        Prioridad prioridad =
                prioridadRepository.findById(id)
                        .orElseThrow();

        try {

            prioridad.setEstado(
                    Estado.valueOf(estado)
            );

            prioridadRepository.save(prioridad);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Estado de prioridad actualizado."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Estado inválido."
            );
        }

        return "redirect:/admin/prioridades";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        if (prioridadRepository.existsById(id)) {

            prioridadRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Prioridad eliminada correctamente."
            );
        }

        return "redirect:/admin/prioridades";
    }
}






