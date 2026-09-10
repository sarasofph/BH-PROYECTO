package com.backhome.demo.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.Persona;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Controller
public class ClienteController {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final SeguimientoRepository seguimientoRepository;

    public ClienteController(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            SeguimientoRepository seguimientoRepository) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    @GetMapping("/cliente/perfil")
    public String perfil(
            Authentication authentication,
            Model model) {

        // Usuario actualmente autenticado
        String email = authentication.getName();

        // Buscar Persona mediante el correo
        Persona persona = personaRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la persona."
                        )
                );

        // Buscar Cliente asociado a esa Persona
        Cliente cliente = clienteRepository
                .findByPersona_IdPersona(persona.getIdPersona())
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró el cliente asociado."
                        )
                );

        // Obtener seguimientos creados por el cliente
        List<Seguimiento> seguimientos =
                seguimientoRepository
                        .findByCliente_IdClienteOrderByIdSeguimientoDesc(
                                cliente.getIdCliente()
                        );

        // Contar seguimientos pendientes de revisión
        long enRevision = seguimientos.stream()
                .filter(s ->
                        s.getEstadoModeracion()
                                == EstadoModeracion.pendiente
                )
                .count();

        // Contar seguimientos rechazados
        long rechazados = seguimientos.stream()
                .filter(s ->
                        s.getEstadoModeracion()
                                == EstadoModeracion.rechazado
                )
                .count();

        // Contar seguimientos verificados
        long verificados = seguimientos.stream()
                .filter(s ->
                        s.getEstadoModeracion()
                                == EstadoModeracion.verificado
                )
                .count();

        // Enviar información a Thymeleaf
        model.addAttribute("persona", persona);
        model.addAttribute("cliente", cliente);

        model.addAttribute(
                "totalSeguimientos",
                seguimientos.size()
        );

        model.addAttribute(
                "enRevision",
                enRevision
        );

        model.addAttribute(
                "rechazados",
                rechazados
        );

        model.addAttribute(
                "verificados",
                verificados
        );

        return "cliente/perfil";
    }
}