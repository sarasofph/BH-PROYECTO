package com.backhome.demo.controller;

import java.util.List;

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
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.model.Seguimiento.EstadoModeracion;
import com.backhome.demo.model.Seguimiento.EstadoSeguimiento;
import com.backhome.demo.repository.AnimalRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.LugarRepository;
import com.backhome.demo.repository.PrioridadRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Controller
@RequestMapping("/admin/seguimientos")
public class AdminSeguimientoController {

    private final SeguimientoRepository seguimientoRepository;
    private final AnimalRepository animalRepository;
    private final ClienteRepository clienteRepository;
    private final LugarRepository lugarRepository;
    private final PrioridadRepository prioridadRepository;

    public AdminSeguimientoController(
            SeguimientoRepository seguimientoRepository,
            AnimalRepository animalRepository,
            ClienteRepository clienteRepository,
            LugarRepository lugarRepository,
            PrioridadRepository prioridadRepository) {

        this.seguimientoRepository = seguimientoRepository;
        this.animalRepository = animalRepository;
        this.clienteRepository = clienteRepository;
        this.lugarRepository = lugarRepository;
        this.prioridadRepository = prioridadRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String moderacion,
            Model model) {

        List<Seguimiento> seguimientos;

        if (buscar != null && !buscar.isBlank()) {

            seguimientos =
                    seguimientoRepository
                            .findByTituloContainingIgnoreCaseOrderByIdSeguimientoDesc(
                                    buscar.trim()
                            );

        } else if (estado != null && !estado.isBlank()) {

            try {

                seguimientos =
                        seguimientoRepository
                                .findByEstadoSeguimientoOrderByIdSeguimientoDesc(
                                        EstadoSeguimiento.valueOf(estado)
                                );

            } catch (IllegalArgumentException e) {

                seguimientos =
                        seguimientoRepository
                                .findAllByOrderByIdSeguimientoDesc();
            }

        } else if (moderacion != null && !moderacion.isBlank()) {

            try {

                seguimientos =
                        seguimientoRepository
                                .findByEstadoModeracionOrderByIdSeguimientoDesc(
                                        EstadoModeracion.valueOf(moderacion)
                                );

            } catch (IllegalArgumentException e) {

                seguimientos =
                        seguimientoRepository
                                .findAllByOrderByIdSeguimientoDesc();
            }

        } else {

            seguimientos =
                    seguimientoRepository
                            .findAllByOrderByIdSeguimientoDesc();
        }

        model.addAttribute("seguimientos", seguimientos);
        model.addAttribute("buscar", buscar == null ? "" : buscar);
        model.addAttribute("estado", estado == null ? "" : estado);
        model.addAttribute(
                "moderacion",
                moderacion == null ? "" : moderacion
        );

        model.addAttribute(
                "estados",
                EstadoSeguimiento.values()
        );

        model.addAttribute(
                "estadosModeracion",
                EstadoModeracion.values()
        );

        return "admin/seguimientos/lista";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Integer id,
            Model model) {

        Seguimiento seguimiento =
                seguimientoRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Seguimiento no encontrado."
                                )
                        );

        model.addAttribute(
                "seguimiento",
                seguimiento
        );

        return "admin/seguimientos/detalle";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute(
                "seguimiento",
                new Seguimiento()
        );

        model.addAttribute(
                "animales",
                animalRepository.findAll()
        );

        model.addAttribute(
                "clientes",
                clienteRepository.findAll()
        );

        model.addAttribute(
                "lugares",
                lugarRepository.findAll()
        );

        model.addAttribute(
                "prioridades",
                prioridadRepository.findAll()
        );

        model.addAttribute(
                "estados",
                EstadoSeguimiento.values()
        );

        return "admin/seguimientos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Seguimiento seguimiento,
            @RequestParam Integer animalId,
            @RequestParam Integer clienteId,
            @RequestParam Integer lugarId,
            @RequestParam(required = false) Integer prioridadId,
            RedirectAttributes redirectAttributes) {

        seguimiento.setAnimal(
                animalRepository.findById(animalId)
                        .orElseThrow()
        );

        seguimiento.setCliente(
                clienteRepository.findById(clienteId)
                        .orElseThrow()
        );

        seguimiento.setLugar(
                lugarRepository.findById(lugarId)
                        .orElseThrow()
        );

        if (prioridadId != null) {

            seguimiento.setPrioridad(
                    prioridadRepository.findById(prioridadId)
                            .orElse(null)
            );

        } else {

            seguimiento.setPrioridad(null);
        }

        if (seguimiento.getEstadoModeracion() == null) {
            seguimiento.setEstadoModeracion(
                    EstadoModeracion.pendiente
            );
        }

        seguimientoRepository.save(seguimiento);

        redirectAttributes.addFlashAttribute(
                "success",
                "Seguimiento guardado correctamente."
        );

        return "redirect:/admin/seguimientos";
    }

    @PostMapping("/{id}/moderacion")
    public String cambiarModeracion(
            @PathVariable Integer id,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes) {

        Seguimiento seguimiento =
                seguimientoRepository.findById(id)
                        .orElseThrow();

        try {

            seguimiento.setEstadoModeracion(
                    EstadoModeracion.valueOf(estado)
            );

            seguimientoRepository.save(seguimiento);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Estado de moderación actualizado."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Estado de moderación inválido."
            );
        }

        return "redirect:/admin/seguimientos/" + id;
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes) {

        Seguimiento seguimiento =
                seguimientoRepository.findById(id)
                        .orElseThrow();

        try {

            seguimiento.setEstadoSeguimiento(
                    EstadoSeguimiento.valueOf(estado)
            );

            seguimientoRepository.save(seguimiento);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Estado del seguimiento actualizado."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Estado inválido."
            );
        }

        return "redirect:/admin/seguimientos/" + id;
    }

    @PostMapping("/{id}/prioridad")
    public String cambiarPrioridad(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer prioridadId,
            RedirectAttributes redirectAttributes) {

        Seguimiento seguimiento =
                seguimientoRepository.findById(id)
                        .orElseThrow();

        if (prioridadId == null) {

            seguimiento.setPrioridad(null);

        } else {

            Prioridad prioridad =
                    prioridadRepository.findById(prioridadId)
                            .orElseThrow();

            seguimiento.setPrioridad(prioridad);
        }

        seguimientoRepository.save(seguimiento);

        redirectAttributes.addFlashAttribute(
                "success",
                "Prioridad actualizada correctamente."
        );

        return "redirect:/admin/seguimientos/" + id;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        if (seguimientoRepository.existsById(id)) {

            seguimientoRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Seguimiento eliminado correctamente."
            );
        }

        return "redirect:/admin/seguimientos";
    }
}