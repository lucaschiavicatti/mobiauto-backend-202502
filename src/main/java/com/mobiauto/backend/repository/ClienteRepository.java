package com.mobiauto.backend.repository;

import com.mobiauto.backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmail(String email);
    List<Cliente> findAllByRevenda_Id(Long revendaId);
}