package com.backhome.demo.controller;

import com.backhome.demo.model.ActualizacionSeguimiento;
import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.EstadoCustodia;
import com.backhome.demo.model.Prioridad;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.model.SeguimientoEncontrado;
import com.backhome.demo.model.SeguimientoPerdido;
import com.backhome.demo.model.Sexo;
import com.backhome.demo.model.Tamano;
import com.backhome.demo.model.TipoSeguimiento;

import com.backhome.demo.repository.ActualizacionSeguimientoRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.EstadoCustodiaRepository;
import com.backhome.demo.repository.LocalidadRepository;
import com.backhome.demo.repository.PrioridadRepository;
import com.backhome.demo.repository.SeguimientoEncontradoRepository;
import com.backhome.demo.repository.SeguimientoPerdidoRepository;
import com.backhome.demo.repository.SeguimientoRepository;

import com.backhome.demo.service.SeguimientoService;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ClienteSeguimientoController {

    private final SeguimientoRepository seguimientoRepository;
    private final ActualizacionSeguimientoRepository actualizacionSeguimientoRepository;
    private final ClienteRepository clienteRepository;
    private final SeguimientoService seguimientoService;
    private final LocalidadRepository localidadRepository;
    private final PrioridadRepository prioridadRepository;
    private final EstadoCustodiaRepository estadoCustodiaRepository;
    private final SeguimientoPerdidoRepository seguimientoPerdidoRepository;
    private final SeguimientoEncontradoRepository seguimientoEncontradoRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ClienteSeguimientoController(
            SeguimientoRepository seguimientoRepository,
            ActualizacionSeguimientoRepository actualizacionSeguimientoRepository,
            ClienteRepository clienteRepository,
            SeguimientoService seguimientoService,
            LocalidadRepository localidadRepository,
            PrioridadRepository prioridadRepository,
            EstadoCustodiaRepository estadoCustodiaRepository,
            SeguimientoPerdidoRepository seguimientoPerdidoRepository,
            SeguimientoEncontradoRepository seguimientoEncontradoRepository) {

        this.seguimientoRepository = seguimientoRepository;
        this.actualizacionSeguimientoRepository = actualizacionSeguimientoRepository;
        this.clienteRepository = clienteRepository;
        this.seguimientoService = seguimientoService;
        this.localidadRepository = localidadRepository;
        this.prioridadRepository = prioridadRepository;
        this.estadoCustodiaRepository = estadoCustodiaRepository;
        this.seguimientoPerdidoRepository = seguimientoPerdidoRepository;
        this.seguimientoEncontradoRepository = seguimientoEncontradoRepository;
    }


    // =========================================================
    // LISTAR MIS SEGUIMIENTOS
    // =========================================================

    @GetMapping("/cliente/seguimientos")
    public String listarSeguimientos(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        Cliente cliente = clienteRepository
                .findByPersonaEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el cliente asociado al usuario."
                        )
                );

        model.addAttribute(
                "seguimientos",
                seguimientoRepository
                        .findByCliente_IdClienteOrderByIdSeguimientoDesc(
                                cliente.getIdCliente()
                        )
        );

        return "cliente/seguimientos";
    }


    // =========================================================
    // MOSTRAR FORMULARIO DE NUEVO SEGUIMIENTO
    // =========================================================

    @GetMapping("/cliente/seguimientos/nuevo")
    public String mostrarFormulario(Model model) {

        cargarDatosFormulario(model);

        return "cliente/seguimiento-form";
    }


    // =========================================================
    // CREAR SEGUIMIENTO
    // =========================================================

    @PostMapping("/cliente/seguimientos")
    public String crearSeguimiento(

            Authentication authentication,
            RedirectAttributes redirectAttributes,

            @RequestParam String titulo,
            @RequestParam String descripcion,
            @RequestParam TipoSeguimiento tipoSeguimiento,
            @RequestParam(required = false) Integer prioridadId,

            // -------------------------
            // DATOS DEL ANIMAL
            // -------------------------

            @RequestParam String nombreAnimal,
            @RequestParam Sexo sexo,
            @RequestParam String color,
            @RequestParam Tamano tamano,
            @RequestParam String descripcionAnimal,

            // -------------------------
            // TIPO DE ANIMAL
            // -------------------------

            @RequestParam String tipoAnimal,
            @RequestParam String especie,
            @RequestParam(required = false) String raza,

            // -------------------------
            // LUGAR
            // -------------------------

            @RequestParam String direccion,
            @RequestParam Integer localidadId,

            // -------------------------
            // PERDIDO
            // -------------------------

            @RequestParam(required = false) String fechaPerdida,
            @RequestParam(required = false) String fechaUltimaVezVisto,

            // -------------------------
            // ENCONTRADO
            // -------------------------

            @RequestParam(required = false) String fechaEncontrado,
            @RequestParam(required = false) Integer estadoCustodiaId) {

        try {

            LocalDateTime fechaPerdidaDate =
                    convertirFecha(fechaPerdida);

            LocalDateTime fechaUltimaVezVistoDate =
                    convertirFecha(fechaUltimaVezVisto);

            LocalDateTime fechaEncontradoDate =
                    convertirFecha(fechaEncontrado);


            seguimientoService.crearSeguimiento(

                    authentication.getName(),

                    titulo,
                    descripcion,
                    tipoSeguimiento,
                    prioridadId,

                    nombreAnimal,
                    sexo,
                    color,
                    tamano,
                    descripcionAnimal,

                    tipoAnimal,
                    especie,
                    raza,

                    direccion,
                    localidadId,

                    fechaPerdidaDate,
                    fechaUltimaVezVistoDate,

                    fechaEncontradoDate,
                    estadoCustodiaId
            );


            redirectAttributes.addFlashAttribute(
                    "exito",
                    "El seguimiento fue creado correctamente."
            );

            return "redirect:/cliente/seguimientos";


        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/cliente/seguimientos/nuevo";
        }
    }


    // =========================================================
    // VER DETALLE DE UN SEGUIMIENTO
    // =========================================================

    @GetMapping("/cliente/seguimientos/{id}")
    public String verDetalle(

            @PathVariable Integer id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            // -------------------------
            // CLIENTE AUTENTICADO
            // -------------------------

            Cliente cliente = clienteRepository
                    .findByPersonaEmailIgnoreCase(
                            authentication.getName()
                    )
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "No se encontró el cliente autenticado."
                            )
                    );


            // -------------------------
            // BUSCAR SEGUIMIENTO
            // -------------------------

            Seguimiento seguimiento =
                    seguimientoRepository.findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "El seguimiento no existe."
                                    )
                            );


            // -------------------------
            // VERIFICAR PROPIETARIO
            // -------------------------

            if (seguimiento.getCliente() == null ||
                    !seguimiento.getCliente()
                            .getIdCliente()
                            .equals(cliente.getIdCliente())) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "No tienes permiso para consultar este seguimiento."
                );

                return "redirect:/cliente/seguimientos";
            }


            // -------------------------
            // SEGUIMIENTO PRINCIPAL
            // -------------------------

            model.addAttribute(
                    "seguimiento",
                    seguimiento
            );


            // -------------------------
            // ACTUALIZACIONES
            // -------------------------

            model.addAttribute(
                    "actualizaciones",
                    actualizacionSeguimientoRepository
                            .findBySeguimiento_IdSeguimientoOrderByCreatedAtDesc(
                                    id
                            )
            );


            // =================================================
            // INFORMACIÓN DE SEGUIMIENTO PERDIDO
            // =================================================

            if (seguimiento.getTipoSeguimiento()
                    == TipoSeguimiento.perdido) {

                seguimientoPerdidoRepository
                        .findBySeguimiento_IdSeguimiento(id)
                        .ifPresent(
                                seguimientoPerdido ->
                                        model.addAttribute(
                                                "seguimientoPerdido",
                                                seguimientoPerdido
                                        )
                        );
            }


            // =================================================
            // INFORMACIÓN DE SEGUIMIENTO ENCONTRADO
            // =================================================

            if (seguimiento.getTipoSeguimiento()
                    == TipoSeguimiento.encontrado) {

                seguimientoEncontradoRepository
                        .findBySeguimiento_IdSeguimiento(id)
                        .ifPresent(
                                seguimientoEncontrado ->
                                        model.addAttribute(
                                                "seguimientoEncontrado",
                                                seguimientoEncontrado
                                        )
                        );
            }


            return "cliente/seguimiento-detalle";


        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/cliente/seguimientos";
        }
    }


    // =========================================================
    // AGREGAR ACTUALIZACIÓN
    // =========================================================

    @PostMapping("/cliente/seguimientos/{id}/actualizaciones")
    public String agregarActualizacion(

            @PathVariable Integer id,
            @RequestParam String mensaje,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {

            // -------------------------
            // CLIENTE AUTENTICADO
            // -------------------------

            Cliente cliente = clienteRepository
                    .findByPersonaEmailIgnoreCase(
                            authentication.getName()
                    )
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "No se encontró el cliente autenticado."
                            )
                    );


            // -------------------------
            // BUSCAR SEGUIMIENTO
            // -------------------------

            Seguimiento seguimiento =
                    seguimientoRepository.findById(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "El seguimiento no existe."
                                    )
                            );


            // -------------------------
            // VERIFICAR PROPIETARIO
            // -------------------------

            if (seguimiento.getCliente() == null ||
                    !seguimiento.getCliente()
                            .getIdCliente()
                            .equals(cliente.getIdCliente())) {

                throw new IllegalStateException(
                        "No tienes permiso para modificar este seguimiento."
                );
            }


            // -------------------------
            // VALIDAR MENSAJE
            // -------------------------

            if (mensaje == null ||
                    mensaje.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "La actualización no puede estar vacía."
                );
            }


            if (mensaje.length() > 2000) {

                throw new IllegalArgumentException(
                        "La actualización no puede superar los 2000 caracteres."
                );
            }


            // -------------------------
            // CREAR ACTUALIZACIÓN
            // -------------------------

            ActualizacionSeguimiento actualizacion =
                    new ActualizacionSeguimiento();

            actualizacion.setSeguimiento(seguimiento);
            actualizacion.setMensaje(mensaje.trim());
            actualizacion.setCreatedAt(LocalDateTime.now());


            actualizacionSeguimientoRepository.save(
                    actualizacion
            );


            redirectAttributes.addFlashAttribute(
                    "exito",
                    "La actualización fue publicada correctamente."
            );


        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }


        return "redirect:/cliente/seguimientos/" + id;
    }


    // =========================================================
    // DATOS PARA EL FORMULARIO
    // =========================================================

    private void cargarDatosFormulario(Model model) {

        model.addAttribute(
                "localidades",
                localidadRepository.findAllByOrderByNombreAsc()
        );


        model.addAttribute(
                "prioridades",
                prioridadRepository.findByEstadoOrderByNivelAsc(
                        Prioridad.Estado.activo
                )
        );


        model.addAttribute(
                "estadosCustodia",
                estadoCustodiaRepository.findAll()
        );


        model.addAttribute(
                "sexos",
                Sexo.values()
        );


        model.addAttribute(
                "tamanos",
                Tamano.values()
        );


        model.addAttribute(
                "tiposSeguimiento",
                TipoSeguimiento.values()
        );
    }


    // =========================================================
    // CONVERTIR FECHA
    // =========================================================

    private LocalDateTime convertirFecha(String fecha) {

        if (fecha == null ||
                fecha.trim().isEmpty()) {

            return null;
        }

        return LocalDateTime.parse(fecha);
    }
}