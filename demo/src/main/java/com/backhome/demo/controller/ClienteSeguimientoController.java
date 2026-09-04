package com.backhome.demo.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.ActualizacionSeguimiento;
import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.repository.ActualizacionSeguimientoRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Controller
@RequestMapping("/cliente/seguimientos")
public class ClienteSeguimientoController {

    private final SeguimientoRepository seguimientoRepository;
    private final ActualizacionSeguimientoRepository actualizacionRepository;
    private final ClienteRepository clienteRepository;

    public ClienteSeguimientoController(
            SeguimientoRepository seguimientoRepository,
            ActualizacionSeguimientoRepository actualizacionRepository,
            ClienteRepository clienteRepository) {
        this.seguimientoRepository = seguimientoRepository;
        this.actualizacionRepository = actualizacionRepository;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping
    public String listar(Authentication authentication, Model model) {
        Cliente cliente = obtenerCliente(authentication);

        List<Seguimiento> seguimientos = seguimientoRepository
                .findByCliente_IdClienteOrderByIdSeguimientoDesc(cliente.getIdCliente());

        model.addAttribute("cliente", cliente);
        model.addAttribute("seguimientos", seguimientos);

        return "cliente/seguimientos";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Integer id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        Cliente cliente = obtenerCliente(authentication);

        Seguimiento seguimiento = seguimientoRepository.findById(id).orElse(null);

        if (seguimiento == null || seguimiento.getCliente() == null
                || !seguimiento.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
            redirectAttributes.addFlashAttribute("error", "No tienes acceso a ese seguimiento.");
            return "redirect:/cliente/seguimientos";
        }

        List<ActualizacionSeguimiento> actualizaciones = actualizacionRepository
                .findBySeguimiento_IdSeguimientoOrderByCreatedAtDesc(id);

        model.addAttribute("seguimiento", seguimiento);
        model.addAttribute("actualizaciones", actualizaciones);
        model.addAttribute("nuevaActualizacion", new ActualizacionSeguimiento());

        return "cliente/seguimiento-detalle";
    }

    @PostMapping("/{id}/actualizaciones")
    public String agregarActualizacion(
            @PathVariable Integer id,
            @RequestParam("mensaje") String mensaje,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Cliente cliente = obtenerCliente(authentication);
        Seguimiento seguimiento = seguimientoRepository.findById(id).orElse(null);

        if (seguimiento == null || seguimiento.getCliente() == null
                || !seguimiento.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
            redirectAttributes.addFlashAttribute("error", "No tienes acceso a ese seguimiento.");
            return "redirect:/cliente/seguimientos";
        }

        if (mensaje == null || mensaje.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La actualización no puede estar vacía.");
            return "redirect:/cliente/seguimientos/" + id;
        }

        ActualizacionSeguimiento actualizacion = new ActualizacionSeguimiento();
        actualizacion.setSeguimiento(seguimiento);
        actualizacion.setMensaje(mensaje.trim());
        actualizacionRepository.save(actualizacion);

        redirectAttributes.addFlashAttribute("exito", "Actualización publicada correctamente.");
        return "redirect:/cliente/seguimientos/" + id;
    }

    private Cliente obtenerCliente(Authentication authentication) {
    return clienteRepository.findByPersonaEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new IllegalStateException(
                    "La cuenta autenticada no está asociada a un cliente."));
}
}
