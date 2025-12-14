package org.example.carshering.identity.domain.repository;

import org.example.carshering.identity.api.dto.request.FilterUserRequest;
import org.example.carshering.identity.domain.model.Client;
import org.example.carshering.identity.domain.model.Document;
import org.example.carshering.identity.domain.valueobject.document.DocumentId;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.domain.valueobject.user.Email;
import org.example.carshering.identity.domain.valueobject.user.Login;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ClientDomainRepository {

    Optional<Client> findById(ClientId id);

    @Transactional
    Client save(Client client); // Сохраняет Client + Documents каскадно!
    Optional<Client> findByEmail(Email email);

    List<Client> findByRoleId(RoleId roleId);

    Page<Client> findClientsWithUnverifiedDocuments(Pageable pageable);

    Page<Document> getAllDocuments(boolean onlyUnverified, Pageable pageable);

    Page<Client> findByFilter(FilterUserRequest filter, Pageable pageable);

    Optional<Client> findByDocumentId(DocumentId documentId);

    Optional<Client> findByLogin(Login of);
}
