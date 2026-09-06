package com.backhome.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estado_custodia")
public class EstadoCustodia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_custodia")
    private Integer idEstadoCustodia;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;


    public Integer getIdEstadoCustodia() {
        return idEstadoCustodia;
    }

    public void setIdEstadoCustodia(Integer idEstadoCustodia) {
        this.idEstadoCustodia = idEstadoCustodia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}