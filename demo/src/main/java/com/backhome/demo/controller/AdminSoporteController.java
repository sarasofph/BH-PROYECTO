package com.backhome.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.repository.MensajeSoporteRepository;

@Controller
@RequestMapping("/admin/soporte")
public class AdminSoporteController {

    private final MensajeSoporteRepository mensajeRepository;

    public AdminSoporteController(
            MensajeSoporteRepository mensajeRepository) {

        this.mensajeRepository = mensajeRepository;
    }

    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
                "mensajes",
                mensajeRepository.findAllByOrderByFechaMensajeDesc()
        );

        return "admin/soporte/lista";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Integer id,
            Model model) {

        model.addAttribute(
                "mensaje",
                mensajeRepository.findById(id)
                        .orElseThrow()
        );

        return "admin/soporte/detalle";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        if (mensajeRepository.existsById(id)) {

            mensajeRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Mensaje eliminado correctamente."
            );
        }

        return "redirect:/admin/soporte";
    }
}
