//package org.example.carshering.mapper;
//
//import org.example.carshering.dto.request.create.CreateCarModelName;
//import org.example.carshering.fleet.api.dto.responce.ModelNameResponse;
//import org.example.carshering.fleet.infrastructure.persistence.entity.Model;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.MappingConstants;
//
//@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
//public abstract class ModelNameMapper {
//
//    public abstract Model toEntity(CreateCarModelName request);
//
//    @Mapping(source = "name", target = "name")
//    public abstract ModelNameResponse toDto(Model entity);
//
//}
