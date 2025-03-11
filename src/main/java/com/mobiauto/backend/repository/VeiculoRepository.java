package com.mobiauto.backend.repository;

import com.mobiauto.backend.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    List<Veiculo> findAllByRevenda_Id(Long revendaId);
}