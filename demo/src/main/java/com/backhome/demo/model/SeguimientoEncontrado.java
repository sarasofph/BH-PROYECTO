package com.backhome.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seguimiento_encontrado")
public class SeguimientoEncontrado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encontrado")
    private Integer idEncontrado;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguimiento_id", nullable = false, unique = true)
    private Seguimiento seguimiento;

    @Column(name = "fecha_encontrado", nullable = false)
    private LocalDateTime fechaEncontrado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_custodia_id", nullable = false)
    private EstadoCustodia estadoCustodia;


    public Integer getIdEncontrado() {
        return idEncontrado;
    }

    public void setIdEncontrado(Integer idEncontrado) {
        this.idEncontrado = idEncontrado;
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

    public EstadoCustodia getEstadoCustodia() {
        return estadoCustodia;
    }

    public void setEstadoCustodia(EstadoCustodia estadoCustodia) {
        this.estadoCustodia = estadoCustodia;
    }
}