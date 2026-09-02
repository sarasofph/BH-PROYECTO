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

import com.backhome.demo.model.Refugio;
import com.backhome.demo.model.Refugio.Estado;
import com.backhome.demo.repository.LocalidadRepository;
import com.backhome.demo.repository.RefugioRepository;

@Controller
@RequestMapping("/admin/refugios")
public class AdminRefugioController {

    private final RefugioRepository refugioRepository;
    private final LocalidadRepository localidadRepository;

    public AdminRefugioController(
            RefugioRepository refugioRepository,
            LocalidadRepository localidadRepository) {

        this.refugioRepository = refugioRepository;
        this.localidadRepository = localidadRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String estado,
            Model model) {

        if (buscar != null && !buscar.isBlank()) {

            model.addAttribute(
                    "refugios",
                    refugioRepository
                            .findByNombreContainingIgnoreCaseOrderByIdRefugioDesc(
                                    buscar.trim()
                            )
            );

        } else if (estado != null && !estado.isBlank()) {

            try {

                model.addAttribute(
                        "refugios",
                        refugioRepository
                                .findByEstadoOrderByIdRefugioDesc(
                                        Estado.valueOf(estado)
                                )
                );

            } catch (IllegalArgumentException e) {

                model.addAttribute(
                        "refugios",
                        refugioRepository
                                .findAllByOrderByIdRefugioDesc()
                );
            }

        } else {

            model.addAttribute(
                    "refugios",
                    refugioRepository
                            .findAllByOrderByIdRefugioDesc()
            );
        }

        model.addAttribute(
                "buscar",
                buscar == null ? "" : buscar
        );

        model.addAttribute(
                "estado",
                estado == null ? "" : estado
        );

        model.addAttribute(
                "estados",
                Estado.values()
        );

        return "admin/refugios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute(
                "refugio",
                new Refugio()
        );

        model.addAttribute(
                "localidades",
                localidadRepository.findAllByOrderByNombreAsc()
        );

        return "admin/refugios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Refugio refugio,
            @RequestParam Integer localidadId,
            RedirectAttributes redirectAttributes) {

        refugio.setLocalidad(
                localidadRepository.findById(localidadId)
                        .orElseThrow()
        );

        if (refugio.getEstado() == null) {
            refugio.setEstado(Estado.activo);
        }

        refugioRepository.save(refugio);

        redirectAttributes.addFlashAttribute(
                "success",
                "Refugio guardado correctamente."
        );

        return "redirect:/admin/refugios";
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Integer id,
            Model model) {

        Refugio refugio =
                refugioRepository.findById(id)
                        .orElseThrow();

        model.addAttribute("refugio", refugio);

        model.addAttribute(
                "localidades",
                localidadRepository.findAllByOrderByNombreAsc()
        );

        return "admin/refugios/formulario";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes) {

        Refugio refugio =
                refugioRepository.findById(id)
                        .orElseThrow();

        try {

            refugio.setEstado(
                    Estado.valueOf(estado)
            );

            refugioRepository.save(refugio);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Estado del refugio actualizado."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Estado inválido."
            );
        }

        return "redirect:/admin/refugios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        if (refugioRepository.existsById(id)) {

            refugioRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Refugio eliminado correctamente."
            );
        }

        return "redirect:/admin/refugios";
    }
}
