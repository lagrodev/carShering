package org.example.carshering.rental.infrastructure.persistence.mapper;

import org.example.carshering.rental.domain.model.Contract;
import org.example.carshering.rental.domain.valueobject.*;
import org.example.carshering.rental.infrastructure.persistence.entity.ContractJpaEntity;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ContractMapper {

    // Domain -> JPA Entity
    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "clientId.value", target = "clientId")
    @Mapping(source = "cardId.value", target = "carId")
    @Mapping(source = "rentalPeriod", target = "period")
    @Mapping(source = "totalCost", target = "totalCost")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "comment", target = "comment")
    public abstract ContractJpaEntity toEntity(Contract contract);

    // JPA Entity -> Domain
    public Contract toDomain(ContractJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Contract.restore(
                entity.getId() != null ? new ContractId(entity.getId()) : null,
                new ClientId(entity.getClientId()),
                new CarId(entity.getCarId()),
                entity.getPeriod(),
                entity.getTotalCost(),
                entity.getState(),
                entity.getComment()
        );
    }
}
