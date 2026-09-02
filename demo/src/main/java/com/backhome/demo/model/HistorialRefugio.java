package com.backhome.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "historial_refugio")
public class HistorialRefugio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial_refugio")
    private Integer idHistorialRefugio;

    @ManyToOne
    @JoinColumn(name = "ingreso_id", nullable = false)
    private IngresoRefugio ingreso;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 30)
    private String estadoNuevo;

    @ManyToOne
    @JoinColumn(name = "administrador_id")
    private Administrador administrador;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "fecha_cambio")
    private LocalDateTime fechaCambio;

    public HistorialRefugio() {
    }

    public Integer getIdHistorialRefugio() {
        return idHistorialRefugio;
    }

    public void setIdHistorialRefugio(Integer idHistorialRefugio) {
        this.idHistorialRefugio = idHistorialRefugio;
    }

    public IngresoRefugio getIngreso() {
        return ingreso;
    }

    public void setIngreso(IngresoRefugio ingreso) {
        this.ingreso = ingreso;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(String estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(LocalDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }
}
