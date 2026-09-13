package com.backhome.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "historial_estado_custodia")
public class HistorialEstadoCustodia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial_custodia")
    private Integer idHistorialCustodia;

    @ManyToOne
    @JoinColumn(name = "seguimiento_encontrado_id", nullable = false)
    private SeguimientoEncontrado seguimientoEncontrado;

    @ManyToOne
    @JoinColumn(name = "estado_custodia_anterior")
    private EstadoCustodia estadoCustodiaAnterior;

    @ManyToOne
    @JoinColumn(name = "estado_custodia_nuevo", nullable = false)
    private EstadoCustodia estadoCustodiaNuevo;

    @ManyToOne
    @JoinColumn(name = "modificado_por")
    private Administrador administrador;

    @Column(name = "fecha_cambio")
    private LocalDateTime fechaCambio;

    public HistorialEstadoCustodia() {
    }

    public Integer getIdHistorialCustodia() {
        return idHistorialCustodia;
    }

    public void setIdHistorialCustodia(Integer idHistorialCustodia) {
        this.idHistorialCustodia = idHistorialCustodia;
    }

    public SeguimientoEncontrado getSeguimientoEncontrado() {
        return seguimientoEncontrado;
    }

    public void setSeguimientoEncontrado(SeguimientoEncontrado seguimientoEncontrado) {
        this.seguimientoEncontrado = seguimientoEncontrado;
    }

    public EstadoCustodia getEstadoCustodiaAnterior() {
        return estadoCustodiaAnterior;
    }

    public void setEstadoCustodiaAnterior(EstadoCustodia estadoCustodiaAnterior) {
        this.estadoCustodiaAnterior = estadoCustodiaAnterior;
    }

    public EstadoCustodia getEstadoCustodiaNuevo() {
        return estadoCustodiaNuevo;
    }

    public void setEstadoCustodiaNuevo(EstadoCustodia estadoCustodiaNuevo) {
        this.estadoCustodiaNuevo = estadoCustodiaNuevo;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(LocalDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }
}