package org.example.carshering.service.impl;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.entity.CarClass;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.mapper.CarClassMapper;
import org.example.carshering.repository.CarClassRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CarClassServiceImpl
 */
@ExtendWith(MockitoExtension.class)
public class CarClassServiceImplTests {

    @Mock
    private CarClassMapper carClassMapper;

    @Mock
    private CarClassRepository carClassRepository;

    @InjectMocks
    private CarClassServiceImpl serviceUnderTest;


    @Test
    @DisplayName("Test createCarClass returns ModelNameResponse after saving car class")
    public void givenCreateCarClassRequest_whenCreateCarClass_thenReturnResponse() {
        // given
        CreateCarModelName request = new CreateCarModelName("Economy");
        CarClass carClass = new CarClass();
        carClass.setId(1L);
        carClass.setName("Economy");

        ModelNameResponse response = new ModelNameResponse("Economy");

        given(carClassMapper.toEntity(request)).willReturn(carClass);
        given(carClassRepository.save(carClass)).willReturn(carClass);
        given(carClassMapper.toDto(carClass)).willReturn(response);

        // when
        ModelNameResponse result = serviceUnderTest.createCarClass(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Economy");

        verify(carClassMapper).toEntity(request);
        verify(carClassRepository).save(carClass);
        verify(carClassMapper).toDto(carClass);
    }

    @Test
    @DisplayName("Test findAllClasses returns list of car class names")
    public void givenExistingCarClasses_whenFindAllClasses_thenReturnListOfNames() {
        // given
        CarClass c1 = new CarClass();
        c1.setName("BMW");
        CarClass c2 = new CarClass();
        c2.setName("Audi");
        CarClass c3 = new CarClass();
        c3.setName("Mercedes");

        given(carClassRepository.findAll()).willReturn(Arrays.asList(c1, c2, c3));

        // when
        List<String> result = serviceUnderTest.findAllClasses();

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0)).isEqualTo("BMW");
        assertThat(result.get(1)).isEqualTo("Audi");
        assertThat(result.get(2)).isEqualTo("Mercedes");

        verify(carClassRepository).findAll();
    }

    @Test
    @DisplayName("Test getCarClassByName returns car class when found")
    public void givenExistingCarClassName_whenGetCarClassByName_thenReturnCarClass() {
        // given
        CarClass carClass = new CarClass();
        carClass.setId(5L);
        carClass.setName("Kia");

        given(carClassRepository.findByNameIgnoreCase("Kia")).willReturn(Optional.of(carClass));

        // when
        CarClass result = serviceUnderTest.getCarClassByName("Kia");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Kia");
        assertThat(result.getId()).isEqualTo(5L);

        verify(carClassRepository).findByNameIgnoreCase("Kia");
    }

    @Test
    @DisplayName("Test getCarClassByName throws NotFoundException when car class not found")
    public void givenNonExistingCarClassName_whenGetCarClassByName_thenThrowNotFoundException() {
        // given
        given(carClassRepository.findByNameIgnoreCase("Unknown")).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.getCarClassByName("Unknown")
        );

        verify(carClassRepository).findByNameIgnoreCase("Unknown");
    }
}