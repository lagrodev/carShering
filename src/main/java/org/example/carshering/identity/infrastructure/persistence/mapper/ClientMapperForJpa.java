package org.example.carshering.identity.infrastructure.persistence.mapper;

import org.example.carshering.identity.domain.model.Client;
import org.example.carshering.identity.domain.model.Document;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.infrastructure.persistence.entity.ClientJpaEntity;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.springframework.stereotype.Component;

@Component
public class ClientMapperForJpa {

    /**
     * JPA Entity -> Domain (с активным документом и ролью)
     *
     * @param entity         JPA entity клиента
     * @param activeDocument активный документ (может быть null)
     * @param role           роль клиента (может быть null)
     * @return Domain модель Client
     */
    public Client toDomain(ClientJpaEntity entity, Document activeDocument, RoleId role) {
        if (entity == null) {
            return null;
        }

        return Client.reconstruct(
                entity.getId() != null ? new ClientId(entity.getId()) : null,
                entity.getFirstName(),
                entity.getLastName(),
                entity.getLogin(),      // Value Object
                entity.getEmail(),      // Value Object
                entity.getPassword(),   // Value Object
                entity.getPhone(),      // Value Object
                entity.isDeleted(),
                entity.isBanned(),
                entity.isEmailVerified(),
                activeDocument,
                role                    // RoleModel
        );
    }

    /**
     * Domain -> JPA Entity (без Role - устанавливается в Repository)
     */
    public ClientJpaEntity toEntity(Client client) {
        if (client == null) {
            return null;
        }

        ClientJpaEntity entity = new ClientJpaEntity();

        if (client.getClientId() != null) {
            entity.setId(client.getClientId().value());
        }
        updateEntity(entity, client);

        return entity;
    }

    /**
     * Обновление существующей JPA entity из Domain модели
     * Используется при UPDATE операциях для оптимизации
     *
     * @param entity существующая JPA entity (managed by Hibernate)
     * @param client Domain модель с новыми данными
     */

    public void updateEntity(ClientJpaEntity entity, Client client) {
        entity.setFirstName(client.getFirstName());
        entity.setLastName(client.getLastName());
        entity.setLogin(client.getLogin());
        entity.setEmail(client.getEmail());
        entity.setPassword(client.getPassword());
        entity.setPhone(client.getPhone());
        entity.setDeleted(client.isDeleted());
        entity.setBanned(client.isBanned());
        entity.setRole(client.getRoleId());
        entity.setEmailVerified(client.isEmailVerified());
    }

}
