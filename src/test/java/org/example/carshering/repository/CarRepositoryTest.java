package org.example.carshering.repository;

import org.example.carshering.entity.Car;
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

    @BeforeEach
    void setUp() {
        carRepository.deleteAll();
    }

    @Test
    @DisplayName("Test")
    public void givenCar_whenSaved_thenIdNotNull() {
        //given
        Car car = Car.builder().model(null).rent(10.).gosNumber("1123").build();

        // when
        carRepository.save(car);
        //then
        assertThat(car.getId()).isNotNull();
    }
}