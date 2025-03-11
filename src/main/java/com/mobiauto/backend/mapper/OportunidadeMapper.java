package com.mobiauto.backend.mapper;

import com.mobiauto.backend.dto.OportunidadeResponseDTO;
import com.mobiauto.backend.model.Cliente;
import com.mobiauto.backend.model.Oportunidade;
import com.mobiauto.backend.model.Veiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OportunidadeMapper {
    OportunidadeMapper INSTANCE = Mappers.getMapper(OportunidadeMapper.class);

    @Mapping(source = "cliente", target = "cliente")
    @Mapping(source = "veiculo", target = "veiculo")
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "revenda.id", target = "revendaId")
    @Mapping(source = "status", target = "status")
    OportunidadeResponseDTO toResponseDTO(Oportunidade oportunidade);

    OportunidadeResponseDTO.ClienteDTO toClienteDTO(Cliente cliente);

    OportunidadeResponseDTO.VeiculoDTO toVeiculoDTO(Veiculo veiculo);
}