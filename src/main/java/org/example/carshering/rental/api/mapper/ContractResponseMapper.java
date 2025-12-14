package org.example.carshering.rental.api.mapper;

import org.example.carshering.fleet.api.dto.responce.CarDetailResponse;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.rental.api.dto.response.ContractResponse;
import org.example.carshering.rental.application.dto.response.ContractDto;
import org.springframework.stereotype.Component;

/**
 * Mapper для сборки ContractResponse из нескольких источников
 * (Contract + Car + Client)
 */
@Component
public class ContractResponseMapper {

    /**
     * Собирает ContractResponse из Contract, Car и Client DTO
     *
     * @param contract данные контракта (только Rental контекст)
     * @param car      данные машины (Car контекст)
     * @param client   данные клиента (Client контекст)
     * @return полный ContractResponse для API
     */
    public ContractResponse toResponse(ContractDto contract, CarDetailResponse car, ClientDto client) {
        return new ContractResponse(
                contract.id(),
                contract.totalCost(),
                car.brand(),
                car.model(),
                car.bodyType(),
                car.carClass(),
                car.yearOfIssue(),
                client.lastName(),
                contract.startDate(),
                contract.endDate(),
                car.vin(),
                car.gosNumber(),
                contract.state()
        );
    }
}
