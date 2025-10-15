package org.example.carshering.mapper;

import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;


@Mapper(componentModel = ComponentModel.SPRING)
public interface ClientMapper {

    UserResponse toDto(Client client);
    Client toEntity(RegistrationRequest userResponse);

}
