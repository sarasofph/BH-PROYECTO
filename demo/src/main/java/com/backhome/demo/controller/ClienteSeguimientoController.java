package com.backhome.demo.controller;

import com.backhome.demo.model.ActualizacionSeguimiento;
import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.EstadoCustodia;
import com.backhome.demo.model.ImagenSeguimiento;
import com.backhome.demo.model.Prioridad;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.model.SeguimientoEncontrado;
import com.backhome.demo.model.SeguimientoPerdido;
import com.backhome.demo.model.Sexo;
import com.backhome.demo.model.Tamano;
import com.backhome.demo.model.TipoSeguimiento;
import com.backhome.demo.model.EstadoSeguimiento;
import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.Localidad;
import com.backhome.demo.model.GestionSeguimiento;
import com.backhome.demo.repository.GestionSeguimientoRepository;



import com.backhome.demo.repository.ActualizacionSeguimientoRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.EstadoCustodiaRepository;
import com.backhome.demo.repository.ImagenSeguimientoRepository;
import com.backhome.demo.repository.LocalidadRepository;
import com.backhome.demo.repository.PrioridadRepository;
import com.backhome.demo.repository.SeguimientoEncontradoRepository;
import com.backhome.demo.repository.SeguimientoPerdidoRepository;
import com.backhome.demo.repository.SeguimientoRepository;

import com.backhome.demo.service.ImagenSeguimientoService;
import com.backhome.demo.service.SeguimientoService;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;

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
    private final ImagenSeguimientoRepository imagenSeguimientoRepository;
    private final ImagenSeguimientoService imagenSeguimientoService;
    private final GestionSeguimientoRepository gestionSeguimientoRepository;


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
            SeguimientoEncontradoRepository seguimientoEncontradoRepository,
            ImagenSeguimientoRepository imagenSeguimientoRepository,
           ImagenSeguimientoService imagenSeguimientoService,
GestionSeguimientoRepository gestionSeguimientoRepository) {

        this.seguimientoRepository = seguimientoRepository;
        this.actualizacionSeguimientoRepository =
                actualizacionSeguimientoRepository;
        this.clienteRepository = clienteRepository;
        this.seguimientoService = seguimientoService;
        this.localidadRepository = localidadRepository;
        this.prioridadRepository = prioridadRepository;
        this.estadoCustodiaRepository = estadoCustodiaRepository;
        this.seguimientoPerdidoRepository =
                seguimientoPerdidoRepository;
        this.seguimientoEncontradoRepository =
                seguimientoEncontradoRepository;
        this.imagenSeguimientoRepository =
        imagenSeguimientoRepository;

this.imagenSeguimientoService =
        imagenSeguimientoService;

this.gestionSeguimientoRepository =
        gestionSeguimientoRepository;
    }


    // =========================================================
    // LISTAR MIS SEGUIMIENTOS
    // =========================================================

    @GetMapping("/cliente/seguimientos")
public String listarSeguimientos(
        Authentication authentication,
        Model model) {

    // =========================================================
    // CLIENTE AUTENTICADO
    // =========================================================

    String email = authentication.getName();

    Cliente cliente = clienteRepository
            .findByPersonaEmailIgnoreCase(email)
            .orElseThrow(() ->
                    new IllegalStateException(
                            "No se encontró el cliente asociado al usuario."
                    )
            );


    // =========================================================
    // OBTENER SEGUIMIENTOS DEL CLIENTE
    // =========================================================

    List<Seguimiento> seguimientos =
            seguimientoRepository
                    .findByCliente_IdClienteOrderByIdSeguimientoDesc(
                            cliente.getIdCliente()
                    );

    model.addAttribute(
            "seguimientos",
            seguimientos
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
            @RequestParam(required = false) Integer estadoCustodiaId,

            // -------------------------
            // IMÁGENES
            // -------------------------

            @RequestParam(
                    value = "imagenes",
                    required = false
            )
            MultipartFile[] imagenes) {

        try {

            LocalDateTime fechaPerdidaDate =
                    convertirFecha(fechaPerdida);

            LocalDateTime fechaUltimaVezVistoDate =
                    convertirFecha(fechaUltimaVezVisto);

            LocalDateTime fechaEncontradoDate =
                    convertirFecha(fechaEncontrado);


            // =================================================
            // CREAR SEGUIMIENTO
            // =================================================

            Seguimiento seguimiento =
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


            // =================================================
            // GUARDAR IMÁGENES
            // =================================================

            imagenSeguimientoService.guardarImagenes(
                    seguimiento,
                    imagenes
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


            // -------------------------
// GESTIÓN DEL ADMINISTRADOR
// -------------------------

List<GestionSeguimiento> gestionesAdmin =
        gestionSeguimientoRepository
                .findBySeguimiento_IdSeguimientoOrderByFechaSeguimientoDesc(
                        id
                );

model.addAttribute(
        "gestionesAdmin",
        gestionesAdmin
);



            // -------------------------
            // IMÁGENES
            // -------------------------

            List<ImagenSeguimiento> imagenes =
                    imagenSeguimientoRepository
                            .findBySeguimiento_IdSeguimiento(id);

            model.addAttribute(
                    "imagenes",
                    imagenes
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


 /* =====================================================
   FORMULARIO EDITAR SEGUIMIENTO
===================================================== */

@GetMapping("/cliente/seguimientos/{id}/editar")
public String editarFormulario(
        @PathVariable Integer id,
        Authentication authentication,
        Model model,
        RedirectAttributes redirectAttributes) {

    String email = authentication.getName();

    Cliente cliente = clienteRepository
            .findByPersonaEmailIgnoreCase(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("Cliente no encontrado")
            );

    Seguimiento seguimiento = seguimientoRepository
            .findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException("Seguimiento no encontrado")
            );

    // Verificar que pertenece al cliente autenticado
    if (seguimiento.getCliente() == null ||
            !seguimiento.getCliente()
                    .getIdCliente()
                    .equals(cliente.getIdCliente())) {

        redirectAttributes.addFlashAttribute(
                "error",
                "No tienes permiso para editar este seguimiento."
        );

        return "redirect:/cliente/seguimientos";
    }

    // No permitir editar casos cerrados o reunidos
    if (seguimiento.getEstadoSeguimiento() == EstadoSeguimiento.cerrado ||
            seguimiento.getEstadoSeguimiento() == EstadoSeguimiento.reunido) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Este seguimiento ya no puede ser editado."
        );

        return "redirect:/cliente/seguimientos/" + id;
    }

    model.addAttribute("seguimiento", seguimiento);

    cargarDatosFormulario(model);

    return "cliente/seguimiento-editar";
}
/* =====================================================
   GUARDAR CAMBIOS DEL SEGUIMIENTO
===================================================== */

@PostMapping("/cliente/seguimientos/{id}/editar")
@Transactional
public String guardarEdicion(
        @PathVariable Integer id,
        Authentication authentication,
        @RequestParam String titulo,
        @RequestParam String descripcion,
        @RequestParam(required = false) Integer prioridadId,
        @RequestParam String direccion,
        @RequestParam Integer localidadId,
        @RequestParam String nombreAnimal,
        @RequestParam Sexo sexo,
        @RequestParam String color,
        @RequestParam Tamano tamano,
        @RequestParam(required = false) String descripcionFisica,
        @RequestParam(required = false) String fechaPerdida,
        @RequestParam(required = false) String fechaUltimaVezVisto,
        @RequestParam(required = false) String fechaEncontrado,
        @RequestParam(required = false) Integer estadoCustodiaId,
        RedirectAttributes redirectAttributes) {

    try {

        String email = authentication.getName();

        Cliente cliente = clienteRepository
                .findByPersonaEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Cliente no encontrado")
                );

        Seguimiento seguimiento = seguimientoRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Seguimiento no encontrado")
                );


        /* =================================================
           VERIFICAR PROPIETARIO
        ================================================== */

        if (seguimiento.getCliente() == null ||
                !seguimiento.getCliente()
                        .getIdCliente()
                        .equals(cliente.getIdCliente())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tienes permiso para editar este seguimiento."
            );

            return "redirect:/cliente/seguimientos";
        }


        /* =================================================
           VERIFICAR ESTADO
        ================================================== */

        if (seguimiento.getEstadoSeguimiento() == EstadoSeguimiento.cerrado ||
                seguimiento.getEstadoSeguimiento() == EstadoSeguimiento.reunido) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Este seguimiento ya no puede ser editado."
            );

            return "redirect:/cliente/seguimientos/" + id;
        }


        /* =================================================
           VALIDACIONES BÁSICAS
        ================================================== */

        if (titulo == null || titulo.trim().isEmpty() ||
                descripcion == null || descripcion.trim().isEmpty() ||
                direccion == null || direccion.trim().isEmpty() ||
                color == null || color.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Completa todos los campos obligatorios."
            );

            return "redirect:/cliente/seguimientos/" + id + "/editar";
        }


        /* =================================================
           ACTUALIZAR SEGUIMIENTO
        ================================================== */

        seguimiento.setTitulo(titulo.trim());
        seguimiento.setDescripcion(descripcion.trim());


        /* =================================================
           PRIORIDAD
        ================================================== */

        if (prioridadId != null) {

            Prioridad prioridad = prioridadRepository
                    .findById(prioridadId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Prioridad no encontrada")
                    );

            seguimiento.setPrioridad(prioridad);

        } else {

            seguimiento.setPrioridad(null);
        }


        /* =================================================
           ACTUALIZAR ANIMAL
        ================================================== */

        seguimiento.getAnimal().setNombre(
                nombreAnimal != null && !nombreAnimal.trim().isEmpty()
                        ? nombreAnimal.trim()
                        : null
        );

        seguimiento.getAnimal().setSexo(sexo);

        seguimiento.getAnimal().setColor(color.trim());

        seguimiento.getAnimal().setTamano(tamano);

        seguimiento.getAnimal().setDescripcion(
                descripcionFisica != null &&
                        !descripcionFisica.trim().isEmpty()
                        ? descripcionFisica.trim()
                        : null
        );


        /* =================================================
           ACTUALIZAR LUGAR
        ================================================== */

        Localidad localidad = localidadRepository
                .findById(localidadId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Localidad no encontrada")
                );

        seguimiento.getLugar().setDireccion(direccion.trim());
        seguimiento.getLugar().setLocalidad(localidad);


        /* =================================================
           ACTUALIZAR DATOS SEGÚN TIPO
        ================================================== */

        if (seguimiento.getTipoSeguimiento() == TipoSeguimiento.perdido) {

            SeguimientoPerdido perdido =
                    seguimientoPerdidoRepository
                            .findBySeguimiento_IdSeguimiento(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Información de pérdida no encontrada"
                                    )
                            );

            if (fechaPerdida != null &&
                    !fechaPerdida.trim().isEmpty()) {

                perdido.setFechaPerdida(
                        convertirFecha(fechaPerdida)
                );
            }

            if (fechaUltimaVezVisto != null &&
                    !fechaUltimaVezVisto.trim().isEmpty()) {

                perdido.setFechaUltimaVezVisto(
                        convertirFecha(fechaUltimaVezVisto)
                );

            } else {

                perdido.setFechaUltimaVezVisto(null);
            }

            seguimientoPerdidoRepository.save(perdido);

        } else {

            SeguimientoEncontrado encontrado =
                    seguimientoEncontradoRepository
                            .findBySeguimiento_IdSeguimiento(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Información del hallazgo no encontrada"
                                    )
                            );

            if (fechaEncontrado != null &&
                    !fechaEncontrado.trim().isEmpty()) {

                encontrado.setFechaEncontrado(
                        convertirFecha(fechaEncontrado)
                );
            }

            if (estadoCustodiaId != null) {

                EstadoCustodia estadoCustodia =
                        estadoCustodiaRepository
                                .findById(estadoCustodiaId)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Estado de custodia no encontrado"
                                        )
                                );

                encontrado.setEstadoCustodia(estadoCustodia);
            }

            seguimientoEncontradoRepository.save(encontrado);
        }


        /* =================================================
           MODERACIÓN
        ================================================== */

        /*
         * Si el seguimiento estaba verificado y el cliente
         * modifica información, vuelve a revisión.
         */

        if (seguimiento.getEstadoModeracion()
                == EstadoModeracion.verificado) {

            seguimiento.setEstadoModeracion(
                    EstadoModeracion.pendiente
            );
        }


        seguimientoRepository.save(seguimiento);


        /* =================================================
           MENSAJE
        ================================================== */

        redirectAttributes.addFlashAttribute(
                "exito",
                "El seguimiento fue actualizado correctamente."
        );

        return "redirect:/cliente/seguimientos/" + id;


    } catch (Exception e) {

        redirectAttributes.addFlashAttribute(
                "error",
                "No se pudieron guardar los cambios: "
                        + e.getMessage()
        );

        return "redirect:/cliente/seguimientos/" + id + "/editar";
    }
}

  /* =====================================================
   ELIMINAR SEGUIMIENTO
===================================================== */

@PostMapping("/cliente/seguimientos/{id}/eliminar")
@Transactional
public String eliminarSeguimiento(
        @PathVariable Integer id,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    String email = authentication.getName();

    Cliente cliente = clienteRepository
            .findByPersonaEmailIgnoreCase(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("Cliente no encontrado")
            );

    Seguimiento seguimiento = seguimientoRepository
            .findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException("Seguimiento no encontrado")
            );

    // Verificar que el seguimiento pertenezca al cliente autenticado
    if (seguimiento.getCliente() == null ||
            !seguimiento.getCliente()
                    .getIdCliente()
                    .equals(cliente.getIdCliente())) {

        redirectAttributes.addFlashAttribute(
                "error",
                "No tienes permiso para eliminar este seguimiento."
        );

        return "redirect:/cliente/seguimientos";
    }

    seguimientoRepository.delete(seguimiento);

    redirectAttributes.addFlashAttribute(
            "exito",
            "El seguimiento fue eliminado correctamente."
    );

    return "redirect:/cliente/seguimientos";
}
}
