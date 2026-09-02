package com.backhome.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "coincidencias",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_coincidencia",
            columnNames = {
                "seguimiento_perdido_id",
                "seguimiento_encontrado_id"
            }
        )
    }
)
public class Coincidencia {

    public enum Estado {
        pendiente,
        confirmada,
        descartada
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_coincidencia")
    private Integer idCoincidencia;

    @ManyToOne
    @JoinColumn(name = "seguimiento_perdido_id", nullable = false)
    private Seguimiento seguimientoPerdido;

    @ManyToOne
    @JoinColumn(name = "seguimiento_encontrado_id", nullable = false)
    private Seguimiento seguimientoEncontrado;

    @Column(
        name = "porcentaje_coincidencia",
        precision = 5,
        scale = 2
    )
    private BigDecimal porcentajeCoincidencia;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado = Estado.pendiente;

    @Column(name = "fecha_coincidencia")
    private LocalDateTime fechaCoincidencia;

    public Coincidencia() {
    }

    public Integer getIdCoincidencia() {
        return idCoincidencia;
    }

    public void setIdCoincidencia(Integer idCoincidencia) {
        this.idCoincidencia = idCoincidencia;
    }

    public Seguimiento getSeguimientoPerdido() {
        return seguimientoPerdido;
    }

    public void setSeguimientoPerdido(Seguimiento seguimientoPerdido) {
        this.seguimientoPerdido = seguimientoPerdido;
    }

    public Seguimiento getSeguimientoEncontrado() {
        return seguimientoEncontrado;
    }

    public void setSeguimientoEncontrado(Seguimiento seguimientoEncontrado) {
        this.seguimientoEncontrado = seguimientoEncontrado;
    }

    public BigDecimal getPorcentajeCoincidencia() {
        return porcentajeCoincidencia;
    }

    public void setPorcentajeCoincidencia(BigDecimal porcentajeCoincidencia) {
        this.porcentajeCoincidencia = porcentajeCoincidencia;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCoincidencia() {
        return fechaCoincidencia;
    }

    public void setFechaCoincidencia(LocalDateTime fechaCoincidencia) {
        this.fechaCoincidencia = fechaCoincidencia;
    }
}
