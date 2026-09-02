package com.backhome.demo.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.Donacion;
import com.backhome.demo.model.Donacion.Estado;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.DonacionRepository;
import com.backhome.demo.repository.RefugioRepository;

@Controller
@RequestMapping("/admin/donaciones")
public class AdminDonacionController {

    private final DonacionRepository donacionRepository;
    private final ClienteRepository clienteRepository;
    private final RefugioRepository refugioRepository;

    public AdminDonacionController(
            DonacionRepository donacionRepository,
            ClienteRepository clienteRepository,
            RefugioRepository refugioRepository) {

        this.donacionRepository = donacionRepository;
        this.clienteRepository = clienteRepository;
        this.refugioRepository = refugioRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String estado,
            Model model) {

        if (estado != null && !estado.isBlank()) {

            try {

                model.addAttribute(
                        "donaciones",
                        donacionRepository
                                .findByEstadoOrderByIdDonacionDesc(
                                        Estado.valueOf(estado)
                                )
                );

            } catch (IllegalArgumentException e) {

                model.addAttribute(
                        "donaciones",
                        donacionRepository
                                .findAllByOrderByIdDonacionDesc()
                );
            }

        } else {

            model.addAttribute(
                    "donaciones",
                    donacionRepository
                            .findAllByOrderByIdDonacionDesc()
            );
        }

        BigDecimal confirmadas =
                donacionRepository.sumarMontoPorEstado(
                        Estado.confirmada
                );

        BigDecimal pendientes =
                donacionRepository.sumarMontoPorEstado(
                        Estado.pendiente
                );

        model.addAttribute(
                "totalConfirmadas",
                confirmadas
        );

        model.addAttribute(
                "totalPendientes",
                pendientes
        );

        model.addAttribute(
                "estados",
                Estado.values()
        );

        model.addAttribute(
                "estado",
                estado == null ? "" : estado
        );

        return "admin/donaciones/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute(
                "donacion",
                new Donacion()
        );

        model.addAttribute(
                "clientes",
                clienteRepository.findAll()
        );

        model.addAttribute(
                "refugios",
                refugioRepository.findAll()
        );

        return "admin/donaciones/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Donacion donacion,
            @RequestParam Integer clienteId,
            @RequestParam(required = false) Integer refugioId,
            RedirectAttributes redirectAttributes) {

        donacion.setCliente(
                clienteRepository.findById(clienteId)
                        .orElseThrow()
        );

        if (refugioId != null) {

            donacion.setRefugio(
                    refugioRepository.findById(refugioId)
                            .orElse(null)
            );

        } else {

            donacion.setRefugio(null);
        }

        if (donacion.getEstado() == null) {
            donacion.setEstado(Estado.pendiente);
        }

        donacionRepository.save(donacion);

        redirectAttributes.addFlashAttribute(
                "success",
                "Donación guardada correctamente."
        );

        return "redirect:/admin/donaciones";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes) {

        Donacion donacion =
                donacionRepository.findById(id)
                        .orElseThrow();

        try {

            donacion.setEstado(
                    Estado.valueOf(estado)
            );

            donacionRepository.save(donacion);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Estado de la donación actualizado."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Estado inválido."
            );
        }

        return "redirect:/admin/donaciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        if (donacionRepository.existsById(id)) {

            donacionRepository.deleteById(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Donación eliminada correctamente."
            );
        }

        return "redirect:/admin/donaciones";
    }
}
