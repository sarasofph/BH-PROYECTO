package com.backhome.demo.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.Consejo;
import com.backhome.demo.repository.ConsejoRepository;

@Controller
@RequestMapping("/admin/consejos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConsejoController {

    private final ConsejoRepository consejoRepository;

    public AdminConsejoController(ConsejoRepository consejoRepository) {
        this.consejoRepository = consejoRepository;
    }

    @GetMapping
    public String listarConsejos(Model model) {

        List<Consejo> consejos =
                consejoRepository.findAllByOrderByIdDesc();

        model.addAttribute("consejos", consejos);

        return "admin/consejos";
    }

    @PostMapping("/crear")
    public String crearConsejo(
            @RequestParam String titulo,
            @RequestParam String descripcion,
            RedirectAttributes redirectAttributes) {

        if (titulo == null || titulo.trim().isEmpty()
                || descripcion == null
                || descripcion.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El título y la descripción son obligatorios."
            );

            return "redirect:/admin/consejos";
        }

        Consejo consejo = new Consejo();

        consejo.setTitulo(titulo.trim());
        consejo.setDescripcion(descripcion.trim());

        consejoRepository.save(consejo);

        redirectAttributes.addFlashAttribute(
                "success",
                "Consejo creado correctamente."
        );

        return "redirect:/admin/consejos";
    }

    @PostMapping("/{id}/editar")
    public String editarConsejo(
            @PathVariable Long id,
            @RequestParam String titulo,
            @RequestParam String descripcion,
            RedirectAttributes redirectAttributes) {

        Consejo consejo =
                consejoRepository.findById(id).orElse(null);

        if (consejo == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El consejo no existe."
            );

            return "redirect:/admin/consejos";
        }

        if (titulo == null || titulo.trim().isEmpty()
                || descripcion == null
                || descripcion.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El título y la descripción son obligatorios."
            );

            return "redirect:/admin/consejos";
        }

        consejo.setTitulo(titulo.trim());
        consejo.setDescripcion(descripcion.trim());

        consejoRepository.save(consejo);

        redirectAttributes.addFlashAttribute(
                "success",
                "Consejo actualizado correctamente."
        );

        return "redirect:/admin/consejos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarConsejo(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (!consejoRepository.existsById(id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El consejo no existe."
            );

            return "redirect:/admin/consejos";
        }

        consejoRepository.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Consejo eliminado correctamente."
        );

        return "redirect:/admin/consejos";
    }
}