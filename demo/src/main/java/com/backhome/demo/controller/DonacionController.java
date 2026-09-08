package com.backhome.demo.controller;

import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.security.core.Authentication;

@Controller
public class DonacionController {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;

    public DonacionController(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/cliente/donar")
    public String mostrarDonacion(
            Authentication authentication,
            Model model) {

        // =====================================================
        // OBTENER USUARIO LOGUEADO
        // =====================================================

        String email = authentication.getName();

        // =====================================================
        // BUSCAR PERSONA POR EMAIL
        // =====================================================

        Persona persona = personaRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la persona."
                        )
                );

        // =====================================================
        // BUSCAR CLIENTE RELACIONADO CON LA PERSONA
        // =====================================================

        Cliente cliente = clienteRepository
                .findByPersona_IdPersona(
                        persona.getIdPersona()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró el cliente."
                        )
                );

        // =====================================================
        // ENVIAR CLIENTE A LA VISTA
        // =====================================================

        model.addAttribute(
                "cliente",
                cliente
        );

        return "cliente/donacion";
    }
}