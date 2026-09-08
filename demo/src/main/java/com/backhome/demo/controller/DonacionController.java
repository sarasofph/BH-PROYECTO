package com.backhome.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.Donacion;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.DonacionRepository;

@Controller
public class DonacionController {

    private final DonacionRepository donacionRepository;
    private final ClienteRepository clienteRepository;

    public DonacionController(
            DonacionRepository donacionRepository,
            ClienteRepository clienteRepository) {

        this.donacionRepository = donacionRepository;
        this.clienteRepository = clienteRepository;
    }

    // =========================================================
    // MOSTRAR FORMULARIO DE DONACIÓN
    // =========================================================

    @GetMapping("/cliente/donar")
    public String mostrarFormularioDonacion(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        Cliente cliente = clienteRepository
                .findByPersonaEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró el cliente asociado al usuario."
                        )
                );

        model.addAttribute("cliente", cliente);

        return "cliente/donacion";
    }

    // =========================================================
    // GUARDAR DONACIÓN
    // =========================================================

    @PostMapping("/cliente/donar")
    public String guardarDonacion(
            Authentication authentication,
            @RequestParam BigDecimal monto,
            @RequestParam(required = false) String mensaje,
            RedirectAttributes redirectAttributes) {

        // -----------------------------------------------------
        // VALIDAR MONTO
        // -----------------------------------------------------

        if (monto == null ||
                monto.compareTo(BigDecimal.ZERO) <= 0) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El monto de la donación debe ser mayor que cero."
            );

            return "redirect:/cliente/donar";
        }

        // -----------------------------------------------------
        // OBTENER CLIENTE LOGUEADO
        // -----------------------------------------------------

        String email = authentication.getName();

        Cliente cliente = clienteRepository
                .findByPersonaEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró el cliente asociado al usuario."
                        )
                );

        // -----------------------------------------------------
        // LIMPIAR MENSAJE
        // -----------------------------------------------------

        if (mensaje != null) {

            mensaje = mensaje.trim();

            if (mensaje.isEmpty()) {
                mensaje = null;
            }

            if (mensaje != null && mensaje.length() > 255) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "El mensaje no puede superar los 255 caracteres."
                );

                return "redirect:/cliente/donar";
            }
        }

        // -----------------------------------------------------
        // CREAR DONACIÓN
        // -----------------------------------------------------

        Donacion donacion = new Donacion();

        donacion.setCliente(cliente);

        donacion.setMonto(monto);

        donacion.setMensaje(mensaje);

        donacion.setFechaDonacion(
                LocalDateTime.now()
        );

        // -----------------------------------------------------
        // GUARDAR
        // -----------------------------------------------------

        donacionRepository.save(donacion);

        // -----------------------------------------------------
        // MENSAJE DE ÉXITO
        // -----------------------------------------------------

        redirectAttributes.addFlashAttribute(
                "success",
                "¡Gracias por tu donación! Tu aporte ha sido registrado correctamente."
        );

        return "redirect:/cliente/donar";
    }
}