package com.backhome.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seguimiento_perdido")
public class SeguimientoPerdido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perdido")
    private Integer idPerdido;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguimiento_id", nullable = false, unique = true)
    private Seguimiento seguimiento;

    @Column(name = "fecha_perdida", nullable = false)
    private LocalDateTime fechaPerdida;

    @Column(name = "fecha_ultima_vez_visto")
    private LocalDateTime fechaUltimaVezVisto;


    public Integer getIdPerdido() {
        return idPerdido;
    }

    public void setIdPerdido(Integer idPerdido) {
        this.idPerdido = idPerdido;
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

    public LocalDateTime getFechaUltimaVezVisto() {
        return fechaUltimaVezVisto;
    }

    public void setFechaUltimaVezVisto(LocalDateTime fechaUltimaVezVisto) {
        this.fechaUltimaVezVisto = fechaUltimaVezVisto;
    }
}