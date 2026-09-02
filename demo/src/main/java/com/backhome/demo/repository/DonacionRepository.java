package com.backhome.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.Donacion;
import com.backhome.demo.model.Donacion.Estado;

@Repository
public interface DonacionRepository extends JpaRepository<Donacion, Integer> {

    List<Donacion> findAllByOrderByIdDonacionDesc();

    List<Donacion> findByEstadoOrderByIdDonacionDesc(
            Estado estado
    );

    List<Donacion> findByCliente_IdClienteOrderByIdDonacionDesc(
            Integer idCliente
    );

    List<Donacion> findByRefugio_IdRefugioOrderByIdDonacionDesc(
            Integer idRefugio
    );

    long countByEstado(Estado estado);

    @Query("""
        SELECT COALESCE(SUM(d.monto), 0)
        FROM Donacion d
        WHERE d.estado = :estado
    """)
    BigDecimal sumarMontoPorEstado(Estado estado);
}