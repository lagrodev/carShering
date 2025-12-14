package org.example.carshering.fleet.domain.repository;

import org.example.carshering.common.domain.valueobject.CarId;
import org.example.carshering.common.domain.valueobject.Money;
import org.example.carshering.fleet.api.dto.responce.MinMaxCellForFilters;
import org.example.carshering.fleet.domain.model.CarDomain;
import org.example.carshering.fleet.domain.valueobject.CarStateType;
import org.example.carshering.fleet.domain.valueobject.Year;
import org.example.carshering.fleet.domain.valueobject.name.BodyType;
import org.example.carshering.fleet.domain.valueobject.name.BrandName;
import org.example.carshering.fleet.domain.valueobject.name.CarClassName;
import org.example.carshering.fleet.domain.valueobject.name.ModelName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CarDomainRepository {
    CarDomain save(CarDomain carModelDomain);

    List<CarDomain> saveAll(List<CarDomain> cars);

    CarDomain findById(CarId carId);


    Page<CarDomain> findAll(Pageable pageable);

    Page<CarDomain> findAll(Pageable pageable,
                            List<BrandName> brands,        // Value Object
                            List<ModelName> models,        // Value Object
                            Year minYear,                   // Value Object
                            Year maxYear,                   // Value Object
                            BodyType bodyType,              // Value Object
                            List<CarClassName> carClasses,  // Value Object
                            List<CarStateType> carStates,   // Enum
                            LocalDateTime dateStart,        // Примитив (стандартный тип)
                            LocalDateTime dateEnd,
                            Money minPrice,                 // Value Object
                            Money maxPrice);

    boolean existsById(Long carId);





    MinMaxCellForFilters getMinMaxCell(
            List<BrandName> brands,
            List<ModelName> models,
            Year minYear,
            Year maxYear,
            BodyType bodyType,
            List<CarClassName> carClasses,
            List<CarStateType> carStates,
            LocalDateTime dateStart,
            LocalDateTime dateEnd

    );

    CarDomain findByIdAndState(CarId id);
}
