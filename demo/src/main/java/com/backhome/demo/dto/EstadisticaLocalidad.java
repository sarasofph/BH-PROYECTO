package com.backhome.demo.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EstadisticaLocalidad {

    private Integer idLocalidad;
    private String nombreLocalidad;
    private Integer poblacion;

    private int reportesPerdidos;
    private int reportesEncontrados;
    private int totalReportes;

    private BigDecimal tasaReportes;

    public EstadisticaLocalidad(
            Integer idLocalidad,
            String nombreLocalidad,
            Integer poblacion) {

        this.idLocalidad = idLocalidad;
        this.nombreLocalidad = nombreLocalidad;
        this.poblacion = poblacion;
        this.reportesPerdidos = 0;
        this.reportesEncontrados = 0;
        this.totalReportes = 0;
        this.tasaReportes = BigDecimal.ZERO;
    }

    public void agregarPerdido() {
        reportesPerdidos++;
        totalReportes++;
        calcularTasa();
    }

    public void agregarEncontrado() {
        reportesEncontrados++;
        totalReportes++;
        calcularTasa();
    }

    private void calcularTasa() {

        if (poblacion != null && poblacion > 0) {

            tasaReportes = BigDecimal.valueOf(totalReportes)
                    .multiply(BigDecimal.valueOf(10000))
                    .divide(
                            BigDecimal.valueOf(poblacion),
                            2,
                            RoundingMode.HALF_UP
                    );
        }
    }

    public Integer getIdLocalidad() {
        return idLocalidad;
    }

    public String getNombreLocalidad() {
        return nombreLocalidad;
    }

    public Integer getPoblacion() {
        return poblacion;
    }

    public int getReportesPerdidos() {
        return reportesPerdidos;
    }

    public int getReportesEncontrados() {
        return reportesEncontrados;
    }

    public int getTotalReportes() {
        return totalReportes;
    }

    public BigDecimal getTasaReportes() {
        return tasaReportes;
    }
}