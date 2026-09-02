package com.backhome.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "seguimiento_perdido")
public class SeguimientoPerdido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento_perdido")
    private Integer idSeguimientoPerdido;

    @OneToOne
    @JoinColumn(name = "seguimiento_id", nullable = false, unique = true)
    private Seguimiento seguimiento;

    @Column(name = "fecha_perdida", nullable = false)
    private LocalDateTime fechaPerdida;

    @Column(name = "ultima_fecha_visto")
    private LocalDateTime ultimaFechaVisto;

    @Column(name = "descripcion_ultima_ubicacion", columnDefinition = "TEXT")
    private String descripcionUltimaUbicacion;

    public SeguimientoPerdido() {
    }

    public Integer getIdSeguimientoPerdido() {
        return idSeguimientoPerdido;
    }

    public void setIdSeguimientoPerdido(Integer idSeguimientoPerdido) {
        this.idSeguimientoPerdido = idSeguimientoPerdido;
    }

    public Seguimiento getSeguimiento() {
        return seguimiento;
    }

    public void setSeguimiento(Seguimiento seguimiento) {
        this.seguimiento = seguimiento;
    }

    public LocalDateTime getFechaPerdida() {
        return fechaPerdida;
    }

    public void setFechaPerdida(LocalDateTime fechaPerdida) {
        this.fechaPerdida = fechaPerdida;
    }

    public LocalDateTime getUltimaFechaVisto() {
        return ultimaFechaVisto;
    }

    public void setUltimaFechaVisto(LocalDateTime ultimaFechaVisto) {
        this.ultimaFechaVisto = ultimaFechaVisto;
    }

    public String getDescripcionUltimaUbicacion() {
        return descripcionUltimaUbicacion;
    }

    public void setDescripcionUltimaUbicacion(String descripcionUltimaUbicacion) {
        this.descripcionUltimaUbicacion = descripcionUltimaUbicacion;
    }
}
