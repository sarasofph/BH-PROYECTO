package com.backhome.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "animal_domestico")
public class AnimalDomestico {

    // =========================================================
    // ID ANIMAL DOMÉSTICO
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_animal_d")
    private Integer idAnimalD;


    // =========================================================
    // ANIMAL
    // animal_domestico.animal_id -> animal.id_animal
    // =========================================================

    @OneToOne
    @JoinColumn(
        name = "animal_id",
        nullable = false,
        unique = true
    )
    private Animal animal;


    // =========================================================
    // ESPECIE
    // =========================================================

    @Column(
        name = "especie",
        nullable = false,
        length = 100
    )
    private String especie;


    // =========================================================
    // RAZA
    // =========================================================

    @Column(
        name = "raza",
        nullable = false,
        length = 100
    )
    private String raza;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AnimalDomestico() {
    }


    // =========================================================
    // GETTER Y SETTER ID
    // =========================================================

    public Integer getIdAnimalD() {
        return idAnimalD;
    }

    public void setIdAnimalD(Integer idAnimalD) {
        this.idAnimalD = idAnimalD;
    }


    // =========================================================
    // GETTER Y SETTER ANIMAL
    // =========================================================

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }


    // =========================================================
    // GETTER Y SETTER ESPECIE
    // =========================================================

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }


    // =========================================================
    // GETTER Y SETTER RAZA
    // =========================================================

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }
}
