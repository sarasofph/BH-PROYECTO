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

import com.backhome.demo.model.Consejo;
import com.backhome.demo.repository.ConsejoRepository;

@Controller
@RequestMapping("/admin/consejos")
public class AdminConsejoController {

    private final ConsejoRepository consejoRepository;

    public AdminConsejoController(
            ConsejoRepository consejoRepository) {

        this.consejoRepository = consejoRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            Model model) {

        if (buscar != null && !buscar.isBlank()) {

            model.addAttribute(
                    "consejos",
                    consejoRepository
                            .findByTituloContainingIgnoreCaseOrderByIdDesc(
                                    buscar.trim()
                            )
            );

        } else {

            model.addAttribute(
                    "consejos",
                    consejoRepository.findAllByOrderByIdDesc()
            );
        }

        model.addAttribute(
                "buscar",
                buscar == null ? "" : buscar
        );

        return "admin/consejos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute(
                "consejo",
                new Consejo()
        );

        return "admin/consejos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Consejo consejo,
            RedirectAttributes redirectAttributes) {

        consejoRepository.save(consejo);

        redirectAttributes.addFlashAttribute(
                "success",
                "Consejo guardado correctamente."
        );

        return "redirect:/admin/consejos";
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "consejo",
                consejoRepository.findById(id)
                        .orElseThrow()
        );

        return "admin/consejos/formulario";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (consejoRepository.existsById(id)) {

            consejoRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Consejo eliminado correctamente."
            );
        }

        return "redirect:/admin/consejos";
    }
}
