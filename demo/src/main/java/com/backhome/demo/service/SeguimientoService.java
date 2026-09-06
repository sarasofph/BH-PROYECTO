package com.backhome.demo.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backhome.demo.model.Animal;
import com.backhome.demo.model.AnimalDomestico;
import com.backhome.demo.model.AnimalExotico;
import com.backhome.demo.model.Cliente;
import com.backhome.demo.model.EstadoCustodia;
import com.backhome.demo.model.EstadoModeracion;
import com.backhome.demo.model.EstadoSeguimiento;
import com.backhome.demo.model.Localidad;
import com.backhome.demo.model.Lugar;
import com.backhome.demo.model.Prioridad;
import com.backhome.demo.model.Seguimiento;
import com.backhome.demo.model.SeguimientoEncontrado;
import com.backhome.demo.model.SeguimientoPerdido;
import com.backhome.demo.model.Sexo;
import com.backhome.demo.model.Tamano;
import com.backhome.demo.model.TipoSeguimiento;

import com.backhome.demo.repository.AnimalDomesticoRepository;
import com.backhome.demo.repository.AnimalExoticoRepository;
import com.backhome.demo.repository.AnimalRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.EstadoCustodiaRepository;
import com.backhome.demo.repository.LocalidadRepository;
import com.backhome.demo.repository.LugarRepository;
import com.backhome.demo.repository.PrioridadRepository;
import com.backhome.demo.repository.SeguimientoEncontradoRepository;
import com.backhome.demo.repository.SeguimientoPerdidoRepository;
import com.backhome.demo.repository.SeguimientoRepository;

@Service
public class SeguimientoService {

    private final SeguimientoRepository seguimientoRepository;
    private final AnimalRepository animalRepository;
    private final AnimalDomesticoRepository animalDomesticoRepository;
    private final AnimalExoticoRepository animalExoticoRepository;
    private final ClienteRepository clienteRepository;
    private final LocalidadRepository localidadRepository;
    private final LugarRepository lugarRepository;
    private final PrioridadRepository prioridadRepository;
    private final SeguimientoPerdidoRepository seguimientoPerdidoRepository;
    private final SeguimientoEncontradoRepository seguimientoEncontradoRepository;
    private final EstadoCustodiaRepository estadoCustodiaRepository;

    public SeguimientoService(
            SeguimientoRepository seguimientoRepository,
            AnimalRepository animalRepository,
            AnimalDomesticoRepository animalDomesticoRepository,
            AnimalExoticoRepository animalExoticoRepository,
            ClienteRepository clienteRepository,
            LocalidadRepository localidadRepository,
            LugarRepository lugarRepository,
            PrioridadRepository prioridadRepository,
            SeguimientoPerdidoRepository seguimientoPerdidoRepository,
            SeguimientoEncontradoRepository seguimientoEncontradoRepository,
            EstadoCustodiaRepository estadoCustodiaRepository) {

        this.seguimientoRepository = seguimientoRepository;
        this.animalRepository = animalRepository;
        this.animalDomesticoRepository = animalDomesticoRepository;
        this.animalExoticoRepository = animalExoticoRepository;
        this.clienteRepository = clienteRepository;
        this.localidadRepository = localidadRepository;
        this.lugarRepository = lugarRepository;
        this.prioridadRepository = prioridadRepository;
        this.seguimientoPerdidoRepository = seguimientoPerdidoRepository;
        this.seguimientoEncontradoRepository = seguimientoEncontradoRepository;
        this.estadoCustodiaRepository = estadoCustodiaRepository;
    }

    @Transactional
    public Seguimiento crearSeguimiento(
            String emailCliente,

            String titulo,
            String descripcion,
            TipoSeguimiento tipoSeguimiento,
            Integer prioridadId,

            String nombreAnimal,
            Sexo sexo,
            String color,
            Tamano tamano,
            String descripcionAnimal,

            String tipoAnimal,
            String especie,
            String raza,

            String direccion,
            Integer localidadId,

            LocalDateTime fechaPerdida,
            LocalDateTime fechaUltimaVezVisto,

            LocalDateTime fechaEncontrado,
            Integer estadoCustodiaId) {

        /*
         * =====================================================
         * 1. OBTENER CLIENTE AUTENTICADO
         * =====================================================
         */

        Cliente cliente = clienteRepository
                .findByPersonaEmailIgnoreCase(emailCliente)
                .orElseThrow(() -> new IllegalStateException(
                        "La cuenta autenticada no está asociada a un cliente."
                ));


        /*
         * =====================================================
         * 2. VALIDAR DATOS PRINCIPALES
         * =====================================================
         */

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                    "El título es obligatorio."
            );
        }

        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción es obligatoria."
            );
        }

        if (tipoSeguimiento == null) {
            throw new IllegalArgumentException(
                    "Debes indicar si el animal está perdido o encontrado."
            );
        }

        if (sexo == null) {
            throw new IllegalArgumentException(
                    "Debes indicar el sexo del animal."
            );
        }

        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException(
                    "El color del animal es obligatorio."
            );
        }

        if (tamano == null) {
            throw new IllegalArgumentException(
                    "Debes indicar el tamaño del animal."
            );
        }


        /*
         * =====================================================
         * 3. CREAR ANIMAL
         * =====================================================
         */

        Animal animal = new Animal();

        animal.setNombre(
                nombreAnimal != null && !nombreAnimal.isBlank()
                        ? nombreAnimal.trim()
                        : null
        );

        animal.setSexo(sexo);
        animal.setColor(color.trim());
        animal.setTamano(tamano);

        if (descripcionAnimal != null
                && !descripcionAnimal.isBlank()) {

            animal.setDescripcion(
                    descripcionAnimal.trim()
            );
        }

        animal = animalRepository.save(animal);


        /*
         * =====================================================
         * 4. CREAR TIPO DE ANIMAL
         * =====================================================
         */

        if (tipoAnimal == null || tipoAnimal.isBlank()) {

            throw new IllegalArgumentException(
                    "Debes indicar si el animal es doméstico o exótico."
            );
        }


        if ("domestico".equalsIgnoreCase(tipoAnimal)) {

            if (especie == null || especie.isBlank()) {

                throw new IllegalArgumentException(
                        "Debes indicar la especie del animal."
                );
            }

            if (raza == null || raza.isBlank()) {

                throw new IllegalArgumentException(
                        "Debes indicar la raza del animal."
                );
            }

            AnimalDomestico domestico =
                    new AnimalDomestico();

            domestico.setAnimal(animal);
            domestico.setEspecie(especie.trim());
            domestico.setRaza(raza.trim());

            animalDomesticoRepository.save(domestico);


        } else if ("exotico".equalsIgnoreCase(tipoAnimal)) {

            if (especie == null || especie.isBlank()) {

                throw new IllegalArgumentException(
                        "Debes indicar la especie del animal."
                );
            }

            AnimalExotico exotico =
                    new AnimalExotico();

            exotico.setAnimal(animal);
            exotico.setEspecie(especie.trim());

            animalExoticoRepository.save(exotico);


        } else {

            throw new IllegalArgumentException(
                    "El tipo de animal debe ser doméstico o exótico."
            );
        }


        /*
         * =====================================================
         * 5. OBTENER LOCALIDAD
         * =====================================================
         */

        if (localidadId == null) {

            throw new IllegalArgumentException(
                    "Debes seleccionar una localidad."
            );
        }

        Localidad localidad =
                localidadRepository.findById(localidadId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La localidad seleccionada no existe."
                                )
                        );


        /*
         * =====================================================
         * 6. CREAR LUGAR
         * =====================================================
         */

        if (direccion == null || direccion.isBlank()) {

            throw new IllegalArgumentException(
                    "La dirección es obligatoria."
            );
        }

        Lugar lugar = new Lugar();

        lugar.setDireccion(direccion.trim());
        lugar.setLocalidad(localidad);

        lugar = lugarRepository.save(lugar);


        /*
         * =====================================================
         * 7. OBTENER PRIORIDAD
         * =====================================================
         */

        Prioridad prioridad = null;

        if (prioridadId != null) {

            prioridad =
                    prioridadRepository.findById(prioridadId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "La prioridad seleccionada no existe."
                                    )
                            );
        }


        /*
         * =====================================================
         * 8. CREAR SEGUIMIENTO
         * =====================================================
         */

        Seguimiento seguimiento = new Seguimiento();

        seguimiento.setTitulo(titulo.trim());
        seguimiento.setDescripcion(descripcion.trim());

        seguimiento.setFechaPublicacion(
                LocalDateTime.now()
        );

        seguimiento.setTipoSeguimiento(
                tipoSeguimiento
        );


        /*
         * ESTADOS AUTOMÁTICOS
         *
         * El cliente NO selecciona estos valores.
         */

        seguimiento.setEstadoSeguimiento(
                EstadoSeguimiento.activo
        );

        seguimiento.setEstadoModeracion(
                EstadoModeracion.pendiente
        );


        /*
         * RELACIONES
         */

        seguimiento.setAnimal(animal);
        seguimiento.setLugar(lugar);
        seguimiento.setCliente(cliente);
        seguimiento.setPrioridad(prioridad);

        seguimiento =
                seguimientoRepository.save(seguimiento);


        /*
         * =====================================================
         * 9. INFORMACIÓN ESPECÍFICA
         * =====================================================
         */

        if (tipoSeguimiento == TipoSeguimiento.perdido) {

            crearSeguimientoPerdido(
                    seguimiento,
                    fechaPerdida,
                    fechaUltimaVezVisto
            );


        } else if (tipoSeguimiento == TipoSeguimiento.encontrado) {

            crearSeguimientoEncontrado(
                    seguimiento,
                    fechaEncontrado,
                    estadoCustodiaId
            );
        }


        /*
         * =====================================================
         * 10. DEVOLVER SEGUIMIENTO CREADO
         * =====================================================
         */

        return seguimiento;
    }


    /*
     * =========================================================
     * CREAR INFORMACIÓN DE ANIMAL PERDIDO
     * =========================================================
     */

    private void crearSeguimientoPerdido(
            Seguimiento seguimiento,
            LocalDateTime fechaPerdida,
            LocalDateTime fechaUltimaVezVisto) {

        if (fechaPerdida == null) {

            throw new IllegalArgumentException(
                    "Debes indicar la fecha en que se perdió el animal."
            );
        }

        SeguimientoPerdido perdido =
                new SeguimientoPerdido();

        perdido.setSeguimiento(seguimiento);
        perdido.setFechaPerdida(fechaPerdida);
        perdido.setFechaUltimaVezVisto(
                fechaUltimaVezVisto
        );

        seguimientoPerdidoRepository.save(perdido);
    }


    /*
     * =========================================================
     * CREAR INFORMACIÓN DE ANIMAL ENCONTRADO
     * =========================================================
     */

    private void crearSeguimientoEncontrado(
            Seguimiento seguimiento,
            LocalDateTime fechaEncontrado,
            Integer estadoCustodiaId) {

        if (fechaEncontrado == null) {

            throw new IllegalArgumentException(
                    "Debes indicar la fecha en que fue encontrado el animal."
            );
        }

        if (estadoCustodiaId == null) {

            throw new IllegalArgumentException(
                    "Debes indicar el estado de custodia."
            );
        }

        EstadoCustodia estadoCustodia =
                estadoCustodiaRepository
                        .findById(estadoCustodiaId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El estado de custodia seleccionado no existe."
                                )
                        );

        SeguimientoEncontrado encontrado =
                new SeguimientoEncontrado();

        encontrado.setSeguimiento(seguimiento);
        encontrado.setFechaEncontrado(fechaEncontrado);
        encontrado.setEstadoCustodia(estadoCustodia);

        seguimientoEncontradoRepository.save(encontrado);
    }
}