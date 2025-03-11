package com.mobiauto.backend.repository;

import com.mobiauto.backend.model.Oportunidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OportunidadeRepository extends JpaRepository<Oportunidade, Long> {
    List<Oportunidade> findAllByRevenda_Id(Long revendaId);

    @Query("SELECT o FROM Oportunidade o WHERE o.usuario.id = :usuarioId AND o.status = 'EM_ATENDIMENTO'")
    List<Oportunidade> findAllEmAtendimentoByUsuarioId(Long usuarioId);
}