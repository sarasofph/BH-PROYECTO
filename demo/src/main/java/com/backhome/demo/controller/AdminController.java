package com.backhome.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.AdministradorRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

@Controller
public class AdminController {

    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;

    public AdminController(
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository) {

        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
    }

    // =========================================================
    // DASHBOARD ADMINISTRADOR
    // =========================================================

    @GetMapping("/admin/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        // -----------------------------------------------------
        // INFORMACIÓN DEL ADMINISTRADOR LOGUEADO
        // -----------------------------------------------------

        if (authentication != null) {

            String email = authentication.getName();

            Persona persona = personaRepository
                    .findByEmailIgnoreCase(email)
                    .orElse(null);

            if (persona != null) {
                model.addAttribute("persona", persona);
            }
        }

        // -----------------------------------------------------
        // USUARIOS
        // -----------------------------------------------------

        long totalUsuarios =
                personaRepository.count();

        long totalClientes =
                clienteRepository.count();

        long totalAdministradores =
                administradorRepository.count();

        long totalActivos =
                personaRepository.countByEstado(
                        EstadoPersona.activo
                );

        long totalBloqueados =
                personaRepository.countByEstado(
                        EstadoPersona.bloqueado
                );

        long totalSuspendidos =
                personaRepository.countByEstado(
                        EstadoPersona.suspendido
                );

        // -----------------------------------------------------
        // ATRIBUTOS DEL DASHBOARD
        // -----------------------------------------------------

        model.addAttribute(
                "totalUsuarios",
                totalUsuarios
        );

        model.addAttribute(
                "totalClientes",
                totalClientes
        );

        model.addAttribute(
                "totalAdministradores",
                totalAdministradores
        );

        model.addAttribute(
                "totalActivos",
                totalActivos
        );

        model.addAttribute(
                "totalBloqueados",
                totalBloqueados
        );

        model.addAttribute(
                "totalSuspendidos",
                totalSuspendidos
        );

        // -----------------------------------------------------
        // TAMBIÉN DEJAMOS LOS NOMBRES ANTERIORES
        // PARA EVITAR ERRORES EN OTRAS VISTAS
        // -----------------------------------------------------

        model.addAttribute(
                "usuariosActivos",
                totalActivos
        );

        model.addAttribute(
                "usuariosBloqueados",
                totalBloqueados
        );

        model.addAttribute(
                "usuariosSuspendidos",
                totalSuspendidos
        );

        // -----------------------------------------------------
        // TOTAL DE USUARIOS NO CLASIFICADOS
        // -----------------------------------------------------

        long usuariosSinEstado =
                totalUsuarios
                        - totalActivos
                        - totalBloqueados
                        - totalSuspendidos;

        if (usuariosSinEstado < 0) {
            usuariosSinEstado = 0;
        }

        model.addAttribute(
                "usuariosSinEstado",
                usuariosSinEstado
        );

        return "admin/dashboard";
    }
}