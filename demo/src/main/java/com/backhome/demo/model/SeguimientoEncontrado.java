package com.backhome.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "seguimiento_encontrado")
public class SeguimientoEncontrado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento_encontrado")
    private Integer idSeguimientoEncontrado;

    @OneToOne
    @JoinColumn(name = "seguimiento_id", nullable = false, unique = true)
    private Seguimiento seguimiento;

    @Column(name = "fecha_encontrado", nullable = false)
    private LocalDateTime fechaEncontrado;

    @Column(name = "descripcion_lugar_encontrado", columnDefinition = "TEXT")
    private String descripcionLugarEncontrado;

    @Column(name = "necesita_refugio")
    private Boolean necesitaRefugio = false;

    @ManyToOne
    @JoinColumn(name = "refugio_id")
    private Refugio refugio;

    public SeguimientoEncontrado() {
    }

    public Integer getIdSeguimientoEncontrado() {
        return idSeguimientoEncontrado;
    }

    public void setIdSeguimientoEncontrado(Integer idSeguimientoEncontrado) {
        this.idSeguimientoEncontrado = idSeguimientoEncontrado;
    }

    public Seguimiento getSeguimiento() {
        return seguimiento;
    }

    public void setSeguimiento(Seguimiento seguimiento) {
        this.seguimiento = seguimiento;
    }

    public LocalDateTime getFechaEncontrado() {
        return fechaEncontrado;
    }

    public void setFechaEncontrado(LocalDateTime fechaEncontrado) {
        this.fechaEncontrado = fechaEncontrado;
    }

    public String getDescripcionLugarEncontrado() {
        return descripcionLugarEncontrado;
    }

    public void setDescripcionLugarEncontrado(String descripcionLugarEncontrado) {
        this.descripcionLugarEncontrado = descripcionLugarEncontrado;
    }

    public Boolean getNecesitaRefugio() {
        return necesitaRefugio;
    }

    public void setNecesitaRefugio(Boolean necesitaRefugio) {
        this.necesitaRefugio = necesitaRefugio;
    }

    public Refugio getRefugio() {
        return refugio;
    }

    public void setRefugio(Refugio refugio) {
        this.refugio = refugio;
    }
}