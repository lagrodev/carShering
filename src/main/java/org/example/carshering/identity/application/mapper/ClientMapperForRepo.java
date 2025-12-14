package org.example.carshering.identity.application.mapper;

import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.application.dto.response.DocumentDto;
import org.example.carshering.identity.application.dto.response.RoleDto;
import org.example.carshering.identity.domain.model.Client;
import org.example.carshering.identity.domain.model.Document;
import org.example.carshering.identity.domain.model.RoleModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ClientMapperForRepo {

    @Mapping(target = "id", source = "clientId.value")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "login", source = "login.value")
    @Mapping(target = "phone", source = "phone.value")
    @Mapping(target = "roleId", source = "roleId.value")
    @Mapping(target = "emailVerified", source = "emailVerified")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "banned", source = "banned")
    @Mapping(target = "password", source = "password.value")
    public abstract ClientDto toDto(Client entity);


    @Mapping(target = "id", source = "documentId.value")
    @Mapping(target = "documentTypeId", source = "documentType.value")
    @Mapping(target = "documentType", expression = "java(null)")
    @Mapping(target = "documentSeries", source = "documentSeries.value")
    @Mapping(target = "documentNumber", source = "documentNumber.value")
    @Mapping(target = "dateOfIssue", source = "dateOfIssue.value")
    @Mapping(target = "issuingAuthority", source = "issuingAuthority.value")
    @Mapping(target = "verified", source = "verified")
    @Mapping(target = "deleted", source = "deleted")
    public abstract DocumentDto toDto(Document entity);


    @Mapping(target = "id", source = "roleId.value")
    @Mapping(target = "name", source = "name.value")
    @Mapping(target = "description", expression = "java(java.util.List.of())")
    public abstract RoleDto toDto(RoleModel entity);

}
