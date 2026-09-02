package com.backhome.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "animal")
public class Animal {

    // =========================================================
    // ENUM SEXO
    // =========================================================

    public enum Sexo {

        macho,
        hembra,
        desconocido
    }


    // =========================================================
    // ENUM TAMAÑO
    // =========================================================

    public enum Tamano {

        pequeño,
        mediano,
        grande
    }


    // =========================================================
    // ID
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_animal")
    private Integer idAnimal;


    // =========================================================
    // NOMBRE
    // =========================================================

    @Column(name = "nombre", length = 100)
    private String nombre;


    // =========================================================
    // SEXO
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;


    // =========================================================
    // COLOR
    // =========================================================

    @Column(name = "color", nullable = false, length = 50)
    private String color;


    // =========================================================
    // TAMAÑO
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "tamano")
    private Tamano tamano;


    // =========================================================
    // DESCRIPCIÓN
    // =========================================================

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Animal() {
    }


    // =========================================================
    // GETTERS Y SETTERS
    // =========================================================

    public Integer getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(Integer idAnimal) {
        this.idAnimal = idAnimal;
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    public Tamano getTamano() {
        return tamano;
    }

    public void setTamano(Tamano tamano) {
        this.tamano = tamano;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}