package org.example.carshering.repository;

import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarModel;
import org.example.carshering.entity.CarState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;
    @Autowired
    private CarStateRepository carStateRepository;

    @Autowired
    private CarModelRepository carModelRepository;
    @BeforeEach
    void setUp() {
        carRepository.deleteAll();
    }



    @Test
    @DisplayName("Test save car functionality")
    public void givenCarObject_whenSave_thenCarIsCreated() {


        CarModel model = CarModel.builder()
                .brand("Toyota")
                .model("Camry")
                .build();

        CarState state = CarState.builder()
                .status("AVAILABLE")
                .build();
        state = carStateRepository.save(state);
        model = carModelRepository.save(model); // ← сохраняем в БД

        //given
        Car carToSave = Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("1HGBH41JXMN109186")
                .gosNumber("1123")
                .build();


        // when
        Car saveCar = carRepository.save(carToSave);

        //then
        assertThat(carToSave).isNotNull();
        assertThat(saveCar).isNotNull();

    }
}