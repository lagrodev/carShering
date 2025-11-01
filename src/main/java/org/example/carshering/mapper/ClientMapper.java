package org.example.carshering.mapper;

import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.response.AllUserResponse;
import org.example.carshering.dto.response.ShortUserResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;


@Mapper(componentModel = ComponentModel.SPRING)
public interface ClientMapper {

    UserResponse toDto(Client client);

    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "banned", target = "banned")
    AllUserResponse toDtoForAdmin(Client client);

    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "banned", target = "banned")
    ShortUserResponse toShortDtoForAdmin(Client client);


    Client toEntity(RegistrationRequest userResponse);

}
