package com.backhome.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.backhome.demo.model.Administrador;
import com.backhome.demo.model.IngresoRefugio;
import com.backhome.demo.model.IngresoRefugio.Estado;
import com.backhome.demo.model.Localidad;
import com.backhome.demo.model.Persona;
import com.backhome.demo.model.Refugio;
import com.backhome.demo.repository.AdministradorRepository;
import com.backhome.demo.repository.IngresoRefugioRepository;
import com.backhome.demo.repository.LocalidadRepository;
import com.backhome.demo.repository.PersonaRepository;
import com.backhome.demo.repository.RefugioRepository;

@Controller
@RequestMapping("/admin/refugios")
public class AdminRefugioController {

    private final RefugioRepository refugioRepository;
    private final LocalidadRepository localidadRepository;
    private final IngresoRefugioRepository ingresoRefugioRepository;
    private final PersonaRepository personaRepository;
    private final AdministradorRepository administradorRepository;

    public AdminRefugioController(
            RefugioRepository refugioRepository,
            LocalidadRepository localidadRepository,
            IngresoRefugioRepository ingresoRefugioRepository,
            PersonaRepository personaRepository,
            AdministradorRepository administradorRepository) {

        this.refugioRepository = refugioRepository;
        this.localidadRepository = localidadRepository;
        this.ingresoRefugioRepository = ingresoRefugioRepository;
        this.personaRepository = personaRepository;
        this.administradorRepository = administradorRepository;
    }

    // =========================================================
    // LISTADO DE REFUGIOS
    // =========================================================

    @GetMapping
    public String listarRefugios(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Integer localidad,
            Model model) {

        boolean tieneBusqueda =
                buscar != null && !buscar.trim().isEmpty();

        List<Refugio> refugios;

        if (tieneBusqueda && localidad != null) {

            refugios = refugioRepository
                    .findByNombreContainingIgnoreCaseAndLocalidad_IdLocalidadOrderByIdRefugioDesc(
                            buscar.trim(),
                            localidad
                    );

        } else if (tieneBusqueda) {

            refugios = refugioRepository
                    .findByNombreContainingIgnoreCaseOrderByIdRefugioDesc(
                            buscar.trim()
                    );

        } else if (localidad != null) {

            refugios = refugioRepository
                    .findByLocalidad_IdLocalidadOrderByIdRefugioDesc(
                            localidad
                    );

        } else {

            refugios = refugioRepository
                    .findAllByOrderByIdRefugioDesc();
        }

        model.addAttribute("refugios", refugios);

        model.addAttribute(
                "localidades",
                localidadRepository.findAllByOrderByNombreAsc()
        );

        model.addAttribute("buscar", buscar);
        model.addAttribute("localidadSeleccionada", localidad);

        model.addAttribute(
                "totalRefugios",
                refugioRepository.count()
        );

        model.addAttribute(
                "ingresosPendientes",
                ingresoRefugioRepository.countByEstado(
                        Estado.pendiente
                )
        );

        return "admin/refugios";
    }

    // =========================================================
    // NUEVO REFUGIO
    // =========================================================

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {

        model.addAttribute("refugio", new Refugio());

        model.addAttribute(
                "localidades",
                localidadRepository.findAllByOrderByNombreAsc()
        );

        model.addAttribute("modoEdicion", false);

        return "admin/refugio-form";
    }

    // =========================================================
    // EDITAR REFUGIO
    // =========================================================

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Refugio refugio =
                refugioRepository.findById(id).orElse(null);

        if (refugio == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró el refugio."
            );

            return "redirect:/admin/refugios";
        }

        model.addAttribute("refugio", refugio);

        model.addAttribute(
                "localidades",
                localidadRepository.findAllByOrderByNombreAsc()
        );

        model.addAttribute("modoEdicion", true);

        return "admin/refugio-form";
    }

    // =========================================================
    // GUARDAR REFUGIO
    // =========================================================

    @PostMapping("/guardar")
    public String guardarRefugio(
            @RequestParam(required = false) Integer idRefugio,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam String direccion,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String email,
            @RequestParam Integer localidadId,
            RedirectAttributes redirectAttributes) {

        Localidad localidad =
                localidadRepository.findById(localidadId).orElse(null);

        if (localidad == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La localidad seleccionada no existe."
            );

            return "redirect:/admin/refugios/nuevo";
        }

        Refugio refugio;

        if (idRefugio == null) {

            refugio = new Refugio();

        } else {

            refugio =
                    refugioRepository.findById(idRefugio).orElse(null);

            if (refugio == null) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "No se encontró el refugio."
                );

                return "redirect:/admin/refugios";
            }
        }

        refugio.setNombre(nombre.trim());

        refugio.setDescripcion(
                descripcion == null || descripcion.trim().isEmpty()
                        ? null
                        : descripcion.trim()
        );

        refugio.setDireccion(direccion.trim());

        refugio.setTelefono(
                telefono == null || telefono.trim().isEmpty()
                        ? null
                        : telefono.trim()
        );

        refugio.setEmail(
                email == null || email.trim().isEmpty()
                        ? null
                        : email.trim()
        );

        refugio.setLocalidad(localidad);

        refugioRepository.save(refugio);

        if (idRefugio == null) {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Refugio registrado correctamente."
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Refugio actualizado correctamente."
            );
        }

        return "redirect:/admin/refugios";
    }

    // =========================================================
    // DETALLE DEL REFUGIO
    // =========================================================

    @GetMapping("/{id}")
    public String verDetalle(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Refugio refugio =
                refugioRepository.findById(id).orElse(null);

        if (refugio == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró el refugio."
            );

            return "redirect:/admin/refugios";
        }

        List<IngresoRefugio> ingresos =
                ingresoRefugioRepository.findIngresosConAnimal(id);

        List<Refugio> recomendados =
                refugioRepository
                        .findByLocalidad_IdLocalidadOrderByIdRefugioDesc(
                                refugio.getLocalidad().getIdLocalidad()
                        )
                        .stream()
                        .filter(r ->
                                !r.getIdRefugio()
                                        .equals(refugio.getIdRefugio())
                        )
                        .toList();

        model.addAttribute("refugio", refugio);
        model.addAttribute("ingresos", ingresos);
        model.addAttribute("recomendados", recomendados);

        model.addAttribute(
                "pendientes",
                ingresos.stream()
                        .filter(i -> i.getEstado() == Estado.pendiente)
                        .count()
        );

        model.addAttribute(
                "confirmados",
                ingresos.stream()
                        .filter(i -> i.getEstado() == Estado.confirmado)
                        .count()
        );

        model.addAttribute(
                "rechazados",
                ingresos.stream()
                        .filter(i -> i.getEstado() == Estado.rechazado)
                        .count()
        );

        return "admin/refugio-detalle";
    }

    // =========================================================
    // REFUGIOS RECOMENDADOS
    // =========================================================

    @GetMapping("/recomendados")
    public String refugiosRecomendados(
            @RequestParam(required = false) Integer localidad,
            Model model) {

        List<Refugio> refugios;

        if (localidad != null) {

            refugios =
                    refugioRepository
                            .findByLocalidad_IdLocalidadOrderByIdRefugioDesc(
                                    localidad
                            );

        } else {

            refugios =
                    refugioRepository
                            .findAllByOrderByIdRefugioDesc();
        }

        model.addAttribute("refugios", refugios);

        model.addAttribute(
                "localidades",
                localidadRepository.findAllByOrderByNombreAsc()
        );

        model.addAttribute(
                "localidadSeleccionada",
                localidad
        );

        return "admin/refugios-recomendados";
    }

    // =========================================================
    // CONFIRMAR INGRESO
    // =========================================================

    @PostMapping("/ingresos/{id}/confirmar")
    public String confirmarIngreso(
            @PathVariable Integer id,
            org.springframework.security.core.Authentication authentication,
            RedirectAttributes redirectAttributes) {

        IngresoRefugio ingreso =
                ingresoRefugioRepository.findById(id).orElse(null);

        if (ingreso == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró el ingreso."
            );

            return "redirect:/admin/refugios";
        }

        Administrador administrador =
                obtenerAdministrador(authentication);

        if (administrador == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible identificar al administrador."
            );

            return "redirect:/admin/refugios/"
                    + ingreso.getRefugio().getIdRefugio();
        }

        ingreso.setEstado(Estado.confirmado);
        ingreso.setAdministrador(administrador);
        ingreso.setFechaVerificacion(LocalDateTime.now());

        ingresoRefugioRepository.save(ingreso);

        redirectAttributes.addFlashAttribute(
                "success",
                "El ingreso fue confirmado correctamente."
        );

        return "redirect:/admin/refugios/"
                + ingreso.getRefugio().getIdRefugio();
    }

    // =========================================================
    // RECHAZAR INGRESO
    // =========================================================

    @PostMapping("/ingresos/{id}/rechazar")
    public String rechazarIngreso(
            @PathVariable Integer id,
            org.springframework.security.core.Authentication authentication,
            RedirectAttributes redirectAttributes) {

        IngresoRefugio ingreso =
                ingresoRefugioRepository.findById(id).orElse(null);

        if (ingreso == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró el ingreso."
            );

            return "redirect:/admin/refugios";
        }

        Administrador administrador =
                obtenerAdministrador(authentication);

        if (administrador == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible identificar al administrador."
            );

            return "redirect:/admin/refugios/"
                    + ingreso.getRefugio().getIdRefugio();
        }

        ingreso.setEstado(Estado.rechazado);
        ingreso.setAdministrador(administrador);
        ingreso.setFechaVerificacion(LocalDateTime.now());

        ingresoRefugioRepository.save(ingreso);

        redirectAttributes.addFlashAttribute(
                "success",
                "El ingreso fue rechazado correctamente."
        );

        return "redirect:/admin/refugios/"
                + ingreso.getRefugio().getIdRefugio();
    }

    // =========================================================
    // OBTENER ADMINISTRADOR ACTUAL
    // =========================================================

    private Administrador obtenerAdministrador(
            org.springframework.security.core.Authentication authentication) {

        if (authentication == null) {
            return null;
        }

        String email = authentication.getName();

        Persona persona =
                personaRepository
                        .findByEmailIgnoreCase(email)
                        .orElse(null);

        if (persona == null) {
            return null;
        }

        return administradorRepository
                .findByPersona_IdPersona(
                        persona.getIdPersona()
                )
                .orElse(null);
    }
}