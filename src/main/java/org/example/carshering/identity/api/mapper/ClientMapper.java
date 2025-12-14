package org.example.carshering.identity.api.mapper;

import org.example.carshering.identity.api.dto.response.AllUserResponse;
import org.example.carshering.identity.api.dto.response.ShortUserResponse;
import org.example.carshering.identity.api.dto.response.UserResponse;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;


@Mapper(componentModel = ComponentModel.SPRING)
public abstract class ClientMapper {

    @Mapping(source = "login", target = "login")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "email", target = "email")
    public abstract UserResponse toDto(ClientDto client);

    @Mapping(source = "banned", target = "banned")
    @Mapping(source = "emailVerified", target = "emailVerified")
    @Mapping(source = "login", target = "login")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    public abstract AllUserResponse toDtoForAdmin(ClientDto client);

    @Mapping(source = "banned", target = "banned")
    @Mapping(source = "emailVerified", target = "emailVerified")
    @Mapping(source = "login", target = "login")
    @Mapping(source = "email", target = "email")
    public abstract ShortUserResponse toShortDtoForAdmin(ClientDto client);

}
