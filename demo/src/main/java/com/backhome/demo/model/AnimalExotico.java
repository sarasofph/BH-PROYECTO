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
@Table(name = "animal_exotico")
public class AnimalExotico {

    // =========================================================
    // ID ANIMAL EXÓTICO
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_animal_e")
    private Integer idAnimalE;


    // =========================================================
    // ANIMAL
    // animal_exotico.animal_id -> animal.id_animal
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
    // CONSTRUCTOR
    // =========================================================

    public AnimalExotico() {
    }


    // =========================================================
    // GETTER Y SETTER ID
    // =========================================================

    public Integer getIdAnimalE() {
        return idAnimalE;
    }

    public void setIdAnimalE(Integer idAnimalE) {
        this.idAnimalE = idAnimalE;
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
}
