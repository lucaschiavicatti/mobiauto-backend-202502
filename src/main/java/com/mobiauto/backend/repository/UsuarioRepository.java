package com.mobiauto.backend.repository;

import com.mobiauto.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    @Query("SELECT u FROM Usuario u WHERE u.revenda.id = :revendaId")
    List<Usuario> findAllByRevendaId(@Param("revendaId") Long revendaId);
    boolean existsByEmail(String email);
}