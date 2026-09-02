package com.backhome.demo.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.IngresoRefugio;
import com.backhome.demo.model.IngresoRefugio.Estado;
import com.backhome.demo.repository.AnimalRepository;
import com.backhome.demo.repository.IngresoRefugioRepository;
import com.backhome.demo.repository.RefugioRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Controller
@RequestMapping("/admin/ingresos-refugio")
public class AdminIngresoRefugioController {

    private final IngresoRefugioRepository ingresoRepository;
    private final RefugioRepository refugioRepository;
    private final AnimalRepository animalRepository;
    private final SeguimientoRepository seguimientoRepository;

    public AdminIngresoRefugioController(
            IngresoRefugioRepository ingresoRepository,
            RefugioRepository refugioRepository,
            AnimalRepository animalRepository,
            SeguimientoRepository seguimientoRepository) {

        this.ingresoRepository = ingresoRepository;
        this.refugioRepository = refugioRepository;
        this.animalRepository = animalRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String estado,
            Model model) {

        if (estado != null && !estado.isBlank()) {

            try {

                model.addAttribute(
                        "ingresos",
                        ingresoRepository
                                .findByEstadoOrderByIdIngresoDesc(
                                        Estado.valueOf(estado)
                                )
                );

            } catch (IllegalArgumentException e) {

                model.addAttribute(
                        "ingresos",
                        ingresoRepository
                                .findAllByOrderByIdIngresoDesc()
                );
            }

        } else {

            model.addAttribute(
                    "ingresos",
                    ingresoRepository
                            .findAllByOrderByIdIngresoDesc()
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

        return "admin/ingresos-refugio/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute(
                "ingreso",
                new IngresoRefugio()
        );

        model.addAttribute(
                "refugios",
                refugioRepository.findAll()
        );

        model.addAttribute(
                "animales",
                animalRepository.findAll()
        );

        model.addAttribute(
                "seguimientos",
                seguimientoRepository.findAll()
        );

        return "admin/ingresos-refugio/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute IngresoRefugio ingreso,
            @RequestParam Integer refugioId,
            @RequestParam Integer animalId,
            @RequestParam Integer seguimientoId,
            RedirectAttributes redirectAttributes) {

        ingreso.setRefugio(
                refugioRepository.findById(refugioId)
                        .orElseThrow()
        );

        ingreso.setAnimal(
                animalRepository.findById(animalId)
                        .orElseThrow()
        );

        ingreso.setSeguimiento(
                seguimientoRepository.findById(seguimientoId)
                        .orElseThrow()
        );

        if (ingreso.getEstado() == null) {
            ingreso.setEstado(Estado.pendiente);
        }

        ingresoRepository.save(ingreso);

        redirectAttributes.addFlashAttribute(
                "success",
                "Ingreso al refugio registrado."
        );

        return "redirect:/admin/ingresos-refugio";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes) {

        IngresoRefugio ingreso =
                ingresoRepository.findById(id)
                        .orElseThrow();

        try {

            Estado nuevoEstado =
                    Estado.valueOf(estado);

            ingreso.setEstado(nuevoEstado);

            if (nuevoEstado == Estado.confirmado ||
                    nuevoEstado == Estado.rechazado) {

                ingreso.setFechaVerificacion(
                        LocalDateTime.now()
                );
            }

            ingresoRepository.save(ingreso);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Estado del ingreso actualizado."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Estado inválido."
            );
        }

        return "redirect:/admin/ingresos-refugio";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        if (ingresoRepository.existsById(id)) {

            ingresoRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Ingreso eliminado correctamente."
            );
        }

        return "redirect:/admin/ingresos-refugio";
    }
}
