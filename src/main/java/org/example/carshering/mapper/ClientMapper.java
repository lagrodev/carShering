package org.example.carshering.mapper;

import org.example.carshering.domain.valueobject.Email;
import org.example.carshering.domain.valueobject.Login;
import org.example.carshering.domain.valueobject.Password;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.response.AllUserResponse;
import org.example.carshering.dto.response.ShortUserResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.domain.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;


@Mapper(componentModel = ComponentModel.SPRING)
public abstract class ClientMapper {

    @Mapping(source = "client.login.value", target = "login")
    @Mapping(source = "client.phone.value", target = "phone")
    @Mapping(source = "client.email.value", target = "email")
    public abstract UserResponse toDto(Client client);

    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "banned", target = "banned")
    @Mapping(source = "emailVerified", target = "emailVerified")
    @Mapping(source = "client.login.value", target = "login")
    @Mapping(source = "client.email.value", target = "email")
    @Mapping(source = "client.phone.value", target = "phone")
    public abstract AllUserResponse toDtoForAdmin(Client client);

    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "banned", target = "banned")
    @Mapping(source = "emailVerified", target = "emailVerified")
    @Mapping(source = "client.login.value", target = "login")
    @Mapping(source = "client.email.value", target = "email")
    public abstract  ShortUserResponse toShortDtoForAdmin(Client client);

    protected Login stringToLogin(String login) {
        return login != null ? Login.of(login) : null;
    }

    protected Email stringToEmail(String email) {
        return email != null ? Email.of(email) : null;
    }

    protected Password stringToPassword(String password) {
        return password != null ? Password.ofEncoded(password) : null;
    }

    public abstract Client toEntity(RegistrationRequest userResponse);

}
