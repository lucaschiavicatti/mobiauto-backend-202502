package com.mobiauto.backend.repository;

import com.mobiauto.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findAllByRevenda_Id(Long revendaId);
}