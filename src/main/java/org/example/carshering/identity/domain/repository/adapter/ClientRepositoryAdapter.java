package org.example.carshering.identity.domain.repository.adapter;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.common.exceptions.custom.BusinessException;
import org.example.carshering.identity.api.dto.request.FilterUserRequest;
import org.example.carshering.identity.domain.model.Client;
import org.example.carshering.identity.domain.model.Document;
import org.example.carshering.identity.domain.model.RoleModel;
import org.example.carshering.identity.domain.repository.ClientDomainRepository;
import org.example.carshering.identity.domain.valueobject.document.DocumentId;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.domain.valueobject.user.Email;
import org.example.carshering.identity.domain.valueobject.user.Login;
import org.example.carshering.identity.infrastructure.persistence.entity.ClientJpaEntity;
import org.example.carshering.identity.infrastructure.persistence.entity.DocumentJpaEntity;
import org.example.carshering.identity.infrastructure.persistence.mapper.ClientMapperForJpa;
import org.example.carshering.identity.infrastructure.persistence.mapper.DocumentMapperForJpa;
import org.example.carshering.identity.infrastructure.persistence.repository.ClientRepository;
import org.example.carshering.identity.infrastructure.persistence.repository.DocumentRepository;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Repository
@Slf4j
public class ClientRepositoryAdapter implements ClientDomainRepository {

    private final DocumentRepository documentRepository;
    private final ClientRepository clientRepository;
    private final ClientMapperForJpa clientMapper;
    private final DocumentMapperForJpa documentMapper;
    private final RoleRepositoryAdapter roleRepository;


    // Сохранение клиента и его активного документа каскадно
    @Override
    @Transactional
    public Client save(Client client) {
        ClientId resultId;

        if (client.getClientId() == null) {
            // создание нового клиента
            ClientJpaEntity entity = clientMapper.toEntity(client);
            ClientJpaEntity savedEntity = clientRepository.save(entity);
            resultId = new ClientId(savedEntity.getId());

        } else {
            resultId = client.getClientId();
            Long clientId = client.getClientId().value();

            ClientJpaEntity entity = clientRepository.findById(clientId).orElseThrow(
                    () -> new BusinessException("Client not found: " + clientId)
            );

            clientMapper.updateEntity(entity, client);
            clientRepository.save(entity);

            handleDocumentUpdate(clientId, client.getActiveDocument());
        }

        return findById(resultId).orElseThrow(
                () -> new BusinessException("Failed to retrieve saved client with ID: " + resultId.value())
        );
    }

    private void handleDocumentUpdate(Long clientId, Document activeDocument) {
        Optional<DocumentJpaEntity> existingDoc = documentRepository.findByClientIdAndDeletedFalse(clientId);

        if (activeDocument == null) {
            existingDoc.ifPresent(doc -> {
                log.info("Soft deleting existing document for client: {}", clientId);
                doc.setDeleted(true);
                documentRepository.save(doc);
            });
        } else if (activeDocument.getDocumentId() == null) {
            // New document being added - first soft delete any existing active document
            if (existingDoc.isPresent()) {
                DocumentJpaEntity doc = existingDoc.get();
                log.info("Soft deleting old document {} before creating new one for client: {}", doc.getId(), clientId);
                doc.setDeleted(true);
                documentRepository.save(doc);
                log.info("Document {} marked as deleted, flushing to database...", doc.getId());
            }

            // Принудительно сбрасываем все изменения в БД перед созданием нового документа
            documentRepository.flush();
            log.info("Flushed all pending changes to database");

            // Now create the new document
            log.info("Creating new document for client: {}", clientId);
            DocumentJpaEntity newDoc = documentMapper.toEntity(activeDocument);
            newDoc.setClientId(clientId);
            DocumentJpaEntity saved = documentRepository.save(newDoc);
            log.info("Saved new document with id: {}", saved.getId());
        } else {
            // Updating existing document
            DocumentJpaEntity docToUpdate = existingDoc.orElseThrow(
                () -> new BusinessException("Document not found for update: " + activeDocument.getDocumentId().value())
            );
            log.info("Updating existing document {} for client: {}", docToUpdate.getId(), clientId);
            documentMapper.updateEntity(docToUpdate, activeDocument);
            documentRepository.save(docToUpdate);
        }
    }

    // поиск клиента
    @Override
    public Optional<Client> findById(ClientId id) {
        Optional<ClientJpaEntity> entity = clientRepository.findByIdWithDocument(id.value());

        return entity.map(this::methodForUpload);
    }


    private Client methodForUpload(ClientJpaEntity e) {
        if (e != null) {
            // Загружаем документ
            DocumentJpaEntity documentEntity = documentRepository.findByClientId(e.getId()).orElse(null);

            // Загружаем роль
            RoleModel role = null;
            if (e.getRole() != null) {
                role = roleRepository.findById(e.getRole()).orElse(null);
            }


            return clientMapper.toDomain(e, documentMapper.toDomain(documentEntity), role != null ? role.getRoleId() : null);
        }
        return null;
    }

    @Override
    public Optional<Client> findByEmail(Email email) {
        Optional<ClientJpaEntity> entity = clientRepository.findByEmailAndDeletedFalse((email.getValue()));

        if (entity.isPresent()) {
            ClientJpaEntity e = entity.get();
            DocumentJpaEntity documentEntity = documentRepository.findByClientId(e.getId()).orElse(null);

            // Загружаем роль
            RoleModel role = null;
            if (e.getRole() != null) {
                role = roleRepository.findById(e.getRole()).orElse(null);
            }


            return Optional.of(clientMapper.toDomain(e, documentMapper.toDomain(documentEntity), role != null ? role.getRoleId() : null));
        }
        return Optional.empty();

    }

    @Override
    public List<Client> findByRoleId(RoleId roleId) {
        List<ClientJpaEntity> entities = clientRepository.findByRoleAndDeletedFalse(roleId);
        if (entities.isEmpty()) {
            return List.of();
        }

        List<Long> clientIds = entities.stream().map(ClientJpaEntity::getId).toList();
        List<DocumentJpaEntity> documents = documentRepository.findByClientIdIn(clientIds);

        Map<Long, DocumentJpaEntity> docMap = documents.stream()
                .collect(toMap(DocumentJpaEntity::getClientId, d -> d));

        // Загружаем роль один раз (все клиенты имеют одну и ту же роль)
        RoleModel role = roleRepository.findById(roleId).orElse(null);

        return entities.stream().map(e -> {
            DocumentJpaEntity doc = docMap.get(e.getId());
            assert role != null;
            return clientMapper.toDomain(e, documentMapper.toDomain(doc), role.getRoleId());
        }).toList();
    }

    @Override
    public Page<Client> findClientsWithUnverifiedDocuments(Pageable pageable) {
        Page<ClientJpaEntity> entities = clientRepository.findClientsWithUnverifiedDocuments(pageable);

        if (entities.isEmpty()) {
            return Page.empty();
        }

        return fetchDocumentsForClients(entities);
    }


    // получение всех документов

    @Override
    public Page<Document> getAllDocuments(boolean onlyUnverified,Pageable pageable) {
        Page<DocumentJpaEntity> entities = documentRepository.findAllForFilter(onlyUnverified, pageable);
        if (!entities.isEmpty()) {
            return entities.map(documentMapper::toDomain);
        }


        return Page.empty();
    }

    @Override
    public Page<Client> findByFilter(FilterUserRequest filter, Pageable pageable){

        Boolean banned = filter.banned();
        RoleId roleId = filter.RoleId() != null ? new RoleId(filter.RoleId()) : null;

        Page<ClientJpaEntity> entities = clientRepository.findByFilter(banned, roleId, pageable);


        if (!entities.isEmpty()) {
            return fetchDocumentsForClients(entities);
        }

        return  Page.empty();
    }

    private Page<Client> fetchDocumentsForClients(Page<ClientJpaEntity> clientEntities) {
        List<Long> clientIds = clientEntities.map(ClientJpaEntity::getId).toList();

        List<DocumentJpaEntity> documents = documentRepository.findByClientIdIn(clientIds);

        Map<Long, DocumentJpaEntity> documentMap = documents.stream()
                .collect(toMap(DocumentJpaEntity::getClientId, doc -> doc));

        return clientEntities.map(e -> {
            DocumentJpaEntity documentEntity = documentMap.get(e.getId());

            // Загружаем роль
            RoleModel role = null;
            if (e.getRole() != null) {
                role = roleRepository.findById(e.getRole()).orElse(null);
            }

            return clientMapper.toDomain(e, documentMapper.toDomain(documentEntity), role != null ? role.getRoleId() : null);
        });
    }

    @Override
    public Optional<Client> findByDocumentId(DocumentId documentId) {
        Optional<DocumentJpaEntity> docEntity = documentRepository.findById(documentId.value());

        if (docEntity.isEmpty()) {
            return Optional.empty();
        }

        Long clientId = docEntity.get().getClientId();
        return findById(new ClientId(clientId));
    }

    @Override
    public Optional<Client> findByLogin(Login of) {
        return clientRepository.findByLoginAndDeletedFalse(of.getValue()).map((this::methodForUpload));
    }


}
