package com.backhome.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "imagenes_seguimiento")
public class ImagenSeguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Integer idImagen;

    @ManyToOne
    @JoinColumn(name = "seguimiento_id", nullable = false)
    private Seguimiento seguimiento;

    @Column(name = "ruta_imagen", nullable = false, length = 255)
    private String rutaImagen;

    @Column(name = "imagen_principal")
    private Boolean imagenPrincipal = false;

    public ImagenSeguimiento() {
    }

    public Integer getIdImagen() {
        return idImagen;
    }

    public void setIdImagen(Integer idImagen) {
        this.idImagen = idImagen;
    }

    public Seguimiento getSeguimiento() {
        return seguimiento;
    }

    public void setSeguimiento(Seguimiento seguimiento) {
        this.seguimiento = seguimiento;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public Boolean getImagenPrincipal() {
        return imagenPrincipal;
    }

    public void setImagenPrincipal(Boolean imagenPrincipal) {
        this.imagenPrincipal = imagenPrincipal;
    }
}
