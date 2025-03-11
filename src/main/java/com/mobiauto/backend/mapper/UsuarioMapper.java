package com.mobiauto.backend.mapper;

import com.mobiauto.backend.dto.UsuarioResponseDTO;
import com.mobiauto.backend.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);

    @Mapping(source = "revenda.id", target = "revendaId")
    @Mapping(target = "cargo", expression = "java(usuario.getCargo())")
    UsuarioResponseDTO toResponseDTO(Usuario usuario);
}