package com.mobiauto.backend.mapper;

import com.mobiauto.backend.dto.RevendaResponseDTO;
import com.mobiauto.backend.model.Revenda;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RevendaMapper {
    RevendaMapper INSTANCE = Mappers.getMapper(RevendaMapper.class);

    RevendaResponseDTO toResponseDTO(Revenda revenda);
}