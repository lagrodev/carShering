package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.CarFilterRequest;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarModel;
import org.example.carshering.entity.CarState;
import org.example.carshering.exceptions.custom.*;
import org.example.carshering.mapper.CarMapper;
import org.example.carshering.repository.CarRepository;
import org.example.carshering.service.domain.CarModelHelperService;
import org.example.carshering.service.domain.CarStateServiceHelper;
import org.example.carshering.service.interfaces.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "gosNumber",
            "yearOfIssue",
            "rent",
            "model.bodyType",
            "model.brand.name",
            "model.model.name",
            "model.carClass.name",
            "car_class",
            "brand",
            "model"
    );

    private static final String CAR_STATE_AVAILABLE = "AVAILABLE";
    private static final String CAR_STATE_UNAVAILABLE = "UNAVAILABLE";

    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final CarModelHelperService carModelService;
    private final CarStateServiceHelper carStateService;


    @Override
    @Transactional
    public CarDetailResponse createCar(CreateCarRequest request) {

        if (carRepository.existsByVin(request.vin())) {
            throw new AlreadyExistsException("VIN already exists");
        }

        if (carRepository.existsByGosNumber(request.gosNumber())) {
            throw new AlreadyExistsException("Gos number already exists");
        }


        Car car = carMapper.toEntity(request);

        CarState state = request.stateId() == null ? carStateService.getDefaultState() : carStateService.getStateById(request.stateId());

        car.setState(state);

        return carMapper.toDetailDto(carRepository.save(car));
    }


    @Override
    @Transactional
    public CarDetailResponse updateCar(Long carId, UpdateCarRequest request) {

        Car car = getCarOrThrow(carId);

        if (!car.getGosNumber().equals(request.gosNumber()) && carRepository.existsByGosNumber(request.gosNumber())) {
            throw new AlreadyExistsException("Gos number already exists");
        }
        if (!car.getVin().equals(request.vin()) && carRepository.existsByVin(request.vin())) {
            throw new AlreadyExistsException("VIN already exists");
        }

        if ((request.vin() != null && request.vin().isBlank()) || (request.gosNumber() != null && request.gosNumber().isBlank())) {
            throw new InvalidDataException("VIN and Gos number cannot be blank");
        }


        carMapper.updateCar(car, request);

        if (request.modelId() == null) {
            return carMapper.toDetailDto(carRepository.save(car));
        }

        CarModel newModel = carModelService.getCarModelById(request.modelId());

        car.setModel(newModel);

        return carMapper.toDetailDto(carRepository.save(car));
    }


    @Override
    public CarDetailResponse getCarById(Long carId) {
        var car = getCarOrThrow(carId);

        return carMapper.toDetailDto(car);
    }

    @Override
    public Car getEntity(Long carId) {
        return getCarOrThrow(carId);
    }

    @Override
    public CarDetailResponse getValidCarById(Long carId) {
        Car car = getCarOrThrow(carId);

        if (!CAR_STATE_AVAILABLE.equalsIgnoreCase(car.getState().getStatus())) {
            throw new StateException("Car not available");
        }

        return carMapper.toDetailDto(car);
    }


    @Override
    @Transactional
    public CarStateResponse updateCarState(Long carId, String carStateName) {
        Car car = getCarOrThrow(carId);

        CarState state = carStateService.getStateByName(carStateName);

        CarModel model = car.getModel();
        if (model.isDeleted() && !CAR_STATE_UNAVAILABLE.equals(state.getStatus())) {
            model.setDeleted(false);
        }

        System.out.println("Updating car state to: " + state.getStatus());

        System.out.println(state.getId());

        car.setState(state);
        carRepository.save(car);
        return new CarStateResponse(state.getId(), carStateName);
    }


    @Override
    @Transactional
    public void deleteCar(Long carId) {
        updateCarState(carId, CAR_STATE_UNAVAILABLE);
    }

    private Car getCarOrThrow(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException("Car not found"));
    }

    private boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    private void validateSortProperties(Sort sort) {
        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new InvalidQueryParameterException(order.getProperty());
            }
        }
    }


    // todo объединить с методом в CarModelServiceImpl - подумать над этим
    // todo сделать дополнительно фильтр по датам, используя ContractService - находить  машины, которые не забронированы и не арендованы в указанный период
    @Override
    public Page<CarListItemResponse> getAllCars(Pageable pageable, CarFilterRequest filter) {
        validateSortProperties(pageable.getSort());

        List<String> brands = isEmpty(filter.brands()) ? null : filter.brands();
        List<String> models = isEmpty(filter.models()) ? null : filter.models();
        List<String> carClasses = isEmpty(filter.carClasses()) ? null : filter.carClasses();
        List<String> carStates = isEmpty(filter.carState()) ? null : filter.carState();

        return carRepository.findByFilter(
                brands,
                models,
                filter.minYear(),
                filter.maxYear(),
                filter.bodyType(),
                carClasses,
                carStates,
                pageable
        ).map(carMapper::toListItemDto);
    }


}
