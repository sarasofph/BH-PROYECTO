package com.backhome.demo.controller;

import java.math.BigDecimal;
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

import com.backhome.demo.model.Donacion;
import com.backhome.demo.repository.DonacionRepository;

@Controller
@RequestMapping("/admin/donaciones")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDonacionController {

    private final DonacionRepository donacionRepository;

    public AdminDonacionController(DonacionRepository donacionRepository) {
        this.donacionRepository = donacionRepository;
    }

    // =========================================================
    // LISTAR DONACIONES
    // =========================================================

    @GetMapping
    public String listarDonaciones(Model model) {

        List<Donacion> donaciones =
                donacionRepository.findAllByOrderByIdDonacionDesc();

        BigDecimal totalDonado =
                donaciones.stream()
                        .map(Donacion::getMonto)
                        .filter(monto -> monto != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("donaciones", donaciones);
        model.addAttribute("totalDonaciones", donaciones.size());
        model.addAttribute("totalDonado", totalDonado);

        return "admin/donacion";
    }

    // =========================================================
    // EDITAR DONACIÓN
    // =========================================================

    @PostMapping("/{id}/editar")
    public String editarDonacion(
            @PathVariable Integer id,
            @RequestParam BigDecimal monto,
            @RequestParam(required = false) String mensaje,
            RedirectAttributes redirectAttributes) {

        Donacion donacion =
                donacionRepository.findById(id).orElse(null);

        if (donacion == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La donación no existe."
            );

            return "redirect:/admin/donaciones";
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El monto debe ser mayor que cero."
            );

            return "redirect:/admin/donaciones";
        }

        donacion.setMonto(monto);
        donacion.setMensaje(mensaje);

        donacionRepository.save(donacion);

        redirectAttributes.addFlashAttribute(
                "success",
                "Donación actualizada correctamente."
        );

        return "redirect:/admin/donaciones";
    }

    // =========================================================
    // ELIMINAR DONACIÓN
    // =========================================================

    @PostMapping("/{id}/eliminar")
    public String eliminarDonacion(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        if (!donacionRepository.existsById(id)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La donación no existe."
            );

            return "redirect:/admin/donaciones";
        }

        donacionRepository.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Donación eliminada correctamente."
        );

        return "redirect:/admin/donaciones";
    }
}