package com.backhome.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.repository.PersonaRepository;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final PersonaRepository personaRepository;

    public AdminDashboardController(
            PersonaRepository personaRepository) {

        this.personaRepository = personaRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        long totalUsuarios =
                personaRepository.count();

        long usuariosActivos =
                personaRepository.countByEstado(
                        EstadoPersona.activo
                );

        long usuariosBloqueados =
                personaRepository.countByEstado(
                        EstadoPersona.bloqueado
                );

        long usuariosSuspendidos =
                personaRepository.countByEstado(
                        EstadoPersona.suspendido
                );

        model.addAttribute(
                "totalUsuarios",
                totalUsuarios
        );

        model.addAttribute(
                "usuariosActivos",
                usuariosActivos
        );

        model.addAttribute(
                "usuariosBloqueados",
                usuariosBloqueados
        );

        model.addAttribute(
                "usuariosSuspendidos",
                usuariosSuspendidos
        );

        return "admin/dashboard";
    }
}