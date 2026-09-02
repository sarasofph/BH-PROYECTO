package com.backhome.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.backhome.demo.model.Animal;
import com.backhome.demo.model.AnimalDomestico;
import com.backhome.demo.model.AnimalExotico;
import com.backhome.demo.repository.AnimalDomesticoRepository;
import com.backhome.demo.repository.AnimalExoticoRepository;
import com.backhome.demo.repository.AnimalRepository;

@Controller
@RequestMapping("/admin/animales")
public class AdminAnimalController {

    private final AnimalRepository animalRepository;
    private final AnimalDomesticoRepository domesticoRepository;
    private final AnimalExoticoRepository exoticoRepository;

    public AdminAnimalController(
            AnimalRepository animalRepository,
            AnimalDomesticoRepository domesticoRepository,
            AnimalExoticoRepository exoticoRepository) {

        this.animalRepository = animalRepository;
        this.domesticoRepository = domesticoRepository;
        this.exoticoRepository = exoticoRepository;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String buscar,
            Model model) {

        List<Animal> animales;

        if (buscar == null || buscar.isBlank()) {
            animales = animalRepository
                    .findAllByOrderByIdAnimalDesc();
        } else {
            animales = animalRepository
                    .findByNombreContainingIgnoreCaseOrderByIdAnimalDesc(
                            buscar.trim()
                    );
        }

        model.addAttribute("animales", animales);
        model.addAttribute("buscar", buscar == null ? "" : buscar);

        return "admin/animales/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute("animal", new Animal());
        model.addAttribute("modo", "crear");

        return "admin/animales/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Animal animal,
            @RequestParam(required = false) String tipoAnimal,
            @RequestParam(required = false) String especie,
            @RequestParam(required = false) String raza,
            Model model) {

        if (animal.getSexo() == null) {
            model.addAttribute(
                    "error",
                    "Debe seleccionar el sexo del animal."
            );

            model.addAttribute("animal", animal);
            model.addAttribute("modo", "crear");

            return "admin/animales/formulario";
        }

        if (animal.getDescripcion() == null ||
                animal.getDescripcion().isBlank()) {

            model.addAttribute(
                    "error",
                    "La descripción es obligatoria."
            );

            model.addAttribute("animal", animal);
            model.addAttribute("modo", "crear");

            return "admin/animales/formulario";
        }

        Animal guardado = animalRepository.save(animal);

        if ("exotico".equalsIgnoreCase(tipoAnimal)) {

            AnimalExotico exotico = new AnimalExotico();

            exotico.setAnimal(guardado);
            exotico.setEspecie(
                    especie == null ? "" : especie.trim()
            );

            exoticoRepository.save(exotico);

        } else {

            AnimalDomestico domestico = new AnimalDomestico();

            domestico.setAnimal(guardado);
            domestico.setEspecie(
                    especie == null ? "" : especie.trim()
            );
            domestico.setRaza(
                    raza == null ? "" : raza.trim()
            );

            domesticoRepository.save(domestico);
        }

        return "redirect:/admin/animales";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Integer id,
            Model model) {

        Animal animal = animalRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Animal no encontrado."
                        )
                );

        AnimalDomestico domestico =
                domesticoRepository
                        .findByAnimal_IdAnimal(id)
                        .orElse(null);

        AnimalExotico exotico =
                exoticoRepository
                        .findByAnimal_IdAnimal(id)
                        .orElse(null);

        model.addAttribute("animal", animal);
        model.addAttribute("domestico", domestico);
        model.addAttribute("exotico", exotico);

        return "admin/animales/detalle";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(
            @PathVariable Integer id) {

        if (!animalRepository.existsById(id)) {
            return "redirect:/admin/animales";
        }

        animalRepository.deleteById(id);

        return "redirect:/admin/animales";
    }
}
