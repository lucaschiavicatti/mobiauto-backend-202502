package com.mobiauto.backend.mapper;

import com.mobiauto.backend.dto.VeiculoResponseDTO;
import com.mobiauto.backend.model.Veiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VeiculoMapper {
    VeiculoMapper INSTANCE = Mappers.getMapper(VeiculoMapper.class);

    @Mapping(source = "revenda.id", target = "revendaId")
    VeiculoResponseDTO toResponseDTO(Veiculo veiculo);
}