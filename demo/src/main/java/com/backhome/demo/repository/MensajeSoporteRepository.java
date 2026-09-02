package com.backhome.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backhome.demo.model.MensajeSoporte;

@Repository
public interface MensajeSoporteRepository
        extends JpaRepository<MensajeSoporte, Integer> {

    List<MensajeSoporte> findAllByOrderByFechaMensajeDesc();

    List<MensajeSoporte> findByCliente_IdClienteOrderByFechaMensajeDesc(
            Integer idCliente
    );
}
