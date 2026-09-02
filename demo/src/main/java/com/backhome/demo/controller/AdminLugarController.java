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

import com.backhome.demo.model.Lugar;
import com.backhome.demo.repository.LocalidadRepository;
import com.backhome.demo.repository.LugarRepository;

@Controller
@RequestMapping("/admin/lugares")
public class AdminLugarController {

    private final LugarRepository lugarRepository;
    private final LocalidadRepository localidadRepository;

    public AdminLugarController(
            LugarRepository lugarRepository,
            LocalidadRepository localidadRepository) {

        this.lugarRepository = lugarRepository;
        this.localidadRepository = localidadRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            Model model) {

        if (buscar != null && !buscar.isBlank()) {

            model.addAttribute(
                    "lugares",
                    lugarRepository
                            .findByDireccionContainingIgnoreCaseOrderByIdLugarDesc(
                                    buscar.trim()
                            )
            );

        } else {

            model.addAttribute(
                    "lugares",
                    lugarRepository
                            .findAllByOrderByIdLugarDesc()
            );
        }

        model.addAttribute(
                "buscar",
                buscar == null ? "" : buscar
        );

        return "admin/lugares/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute(
                "lugar",
                new Lugar()
        );

        model.addAttribute(
                "localidades",
                localidadRepository.findAllByOrderByNombreAsc()
        );

        return "admin/lugares/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Lugar lugar,
            @RequestParam Integer localidadId,
            RedirectAttributes redirectAttributes) {

        lugar.setLocalidad(
                localidadRepository.findById(localidadId)
                        .orElseThrow()
        );

        lugarRepository.save(lugar);

        redirectAttributes.addFlashAttribute(
                "success",
                "Lugar guardado correctamente."
        );

        return "redirect:/admin/lugares";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        try {

            if (lugarRepository.existsById(id)) {

                lugarRepository.deleteById(id);

                redirectAttributes.addFlashAttribute(
                        "success",
                        "Lugar eliminado correctamente."
                );
            }

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede eliminar este lugar porque está siendo utilizado por otros registros."
            );
        }

        return "redirect:/admin/lugares";
    }
}
