package com.mobiauto.backend.repository;

import com.mobiauto.backend.model.Oportunidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OportunidadeRepository extends JpaRepository<Oportunidade, Long> {
    List<Oportunidade> findByRevendaId(Long revendaId);
}