package com.mobiauto.backend.mapper;

import com.mobiauto.backend.dto.ClienteResponseDTO;
import com.mobiauto.backend.model.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ClienteMapper {
    ClienteMapper INSTANCE = Mappers.getMapper(ClienteMapper.class);

    @Mapping(source = "revenda.id", target = "revendaId")
    ClienteResponseDTO toResponseDTO(Cliente cliente);
}