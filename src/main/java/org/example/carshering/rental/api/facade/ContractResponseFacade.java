package org.example.carshering.rental.api.facade;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.fleet.api.dto.responce.CarDetailResponse;
import org.example.carshering.fleet.api.facade.CarFacade;
import org.example.carshering.fleet.application.dto.response.CarDto;
import org.example.carshering.fleet.application.service.CarApplicationService;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.application.service.ClientApplicationService;
import org.example.carshering.rental.api.dto.response.ContractResponse;
import org.example.carshering.rental.api.mapper.ContractResponseMapper;
import org.example.carshering.rental.application.dto.response.ContractDto;
import org.example.carshering.service.interfaces.CarService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Facade для сборки ContractResponse из нескольких контекстов
 * (Rental + Car + Client)
 * <p>
 * Изолирует ContractApplicationService от зависимостей CarService/ClientService
 */
@Component
@RequiredArgsConstructor
public class ContractResponseFacade {
    private final CarApplicationService carService;
    private final CarFacade carFacade;
    private final ClientApplicationService clientService;
    private final ContractResponseMapper responseMapper;

    /**
     * Универсальный метод для получения ContractResponse по ContractDto
     * Проверка владельца должна быть выполнена ДО вызова этого метода
     * (на уровне Application Service)
     *
     * @param contract DTO контракта из Application Service
     * @return полный ContractResponse с данными Car и Client
     */
    public ContractResponse getContractResponse(ContractDto contract) {
        // Загружаем Car и Client
        CarDetailResponse car = carFacade.toDetailResponse( carService.getCarById(new CarId(contract.carId())));
        ClientDto client = clientService.findUser(contract.clientId());

        // Собираем Response через mapper
        return responseMapper.toResponse(contract, car, client);
    }

    public BigDecimal getCarPricePerHour(Long carId) {
        CarDto carDto = carService.getCarById(new CarId(carId));
        return carDto.dailyRate();
    }

    /**
     * Для списка контрактов (пока без batch loading)
     * TODO: добавить batch loading когда в CarService/ClientService появятся методы getByIds
     *
     * @param contracts страница с ContractDto
     * @return страница с ContractResponse
     */
    public Page<ContractResponse> getContractsResponses(Page<ContractDto> contracts) {
        // Пока без batch loading - просто маппим каждый контракт
        return contracts.map(this::getContractResponse);
    }

}
