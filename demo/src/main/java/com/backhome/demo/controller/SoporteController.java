package com.backhome.demo.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.MensajeSoporte;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.MensajeSoporteRepository;

import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/cliente/soporte")
public class SoporteController {

    private final MensajeSoporteRepository mensajeSoporteRepository;
    private final ClienteRepository clienteRepository;

    public SoporteController(
            MensajeSoporteRepository mensajeSoporteRepository,
            ClienteRepository clienteRepository) {

        this.mensajeSoporteRepository = mensajeSoporteRepository;
        this.clienteRepository = clienteRepository;
    }

  @GetMapping
public String mostrarSoporte(
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    if (authentication == null || !authentication.isAuthenticated()) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Debes iniciar sesión para comunicarte con el equipo de BackHome."
        );

        return "redirect:/login";
    }

    return "cliente/soporte";
}

    @PostMapping
    public String enviarMensaje(
            @RequestParam String mensaje,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (mensaje == null || mensaje.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debes escribir un mensaje."
            );

            return "redirect:/cliente/soporte";
        }

        Cliente cliente = clienteRepository
                .findByPersonaEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el cliente."
                        )
                );

        MensajeSoporte mensajeSoporte = new MensajeSoporte();

        mensajeSoporte.setCliente(cliente);
        mensajeSoporte.setMensajeCliente(mensaje.trim());
        mensajeSoporte.setFechaMensaje(LocalDateTime.now());

        mensajeSoporteRepository.save(mensajeSoporte);

        redirectAttributes.addFlashAttribute(
                "exito",
                "Tu mensaje fue enviado correctamente."
        );

        return "redirect:/cliente/soporte";
    }
}