package org.example.carshering.service.impl;

import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.entity.CarState;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.mapper.CarStateMapper;
import org.example.carshering.repository.CarStateRepository;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarStateServiceImplTest {

    @Mock
    private CarStateMapper stateMapper;

    private DataUtils dataUtils = new DataUtils();




    @Mock
    private CarStateRepository carStateRepository;

    @InjectMocks
    private  CarStateServiceImpl serviceUnderTest;

    @Test
    @DisplayName("Test get сarState by id functionality")
    public void givenId_whenGetById_thenCarStateIsReturned(){

        // given
        CarState carState = CarState.builder().status("STATE").build();
        String string = "STATE";

        given(carStateRepository.findByStatusIgnoreCase(string)).willReturn(Optional.ofNullable(carState));

        // when


        CarState actual = serviceUnderTest.getStateByName(string);

        // then

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(string);
        verify(carStateRepository).findByStatusIgnoreCase(string);
    }

    @Test
    @DisplayName("givenIncorrectStateName_whenGetStateByName_thenThrowNotFoundException")
    public void givenIncorrectStateName_whenGetStateByName_thenThrowNotFoundException() {
        // given
        String invalidState = "UNKNOWN_STATE";
        given(carStateRepository.findByStatusIgnoreCase(anyString()))
                .willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.getStateByName(invalidState),
                "State not found"
        );

        verify(carStateRepository).findByStatusIgnoreCase(invalidState);
    }

    @Test
    @DisplayName("givenExistingStates_whenGetAllStates_thenReturnMappedDtoList")
    public void givenExistingStates_whenGetAllStates_thenReturnMappedDtoList() {
        // given
        CarState available = new CarState();
        available.setId(1L);
        available.setStatus("AVAILABLE");

        CarState rented = new CarState();
        rented.setId(2L);
        rented.setStatus("RENTED");

        List<CarState> carStates = List.of(available, rented);

        CarStateResponse availableDto = new CarStateResponse(1L, "AVAILABLE");
        CarStateResponse rentedDto = new CarStateResponse(2L, "RENTED");

        given(carStateRepository.findAll()).willReturn(carStates);
        given(stateMapper.toDto(available)).willReturn(availableDto);
        given(stateMapper.toDto(rented)).willReturn(rentedDto);

        // when
        List<CarStateResponse> result = serviceUnderTest.getAllStates();

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(CarStateResponse::status)
                .containsExactlyInAnyOrder("AVAILABLE", "RENTED");

        verify(carStateRepository).findAll();
        verify(stateMapper, times(2)).toDto(any(CarState.class));
    }


    @Test
    @DisplayName("givenNoStates_whenGetAllStates_thenReturnEmptyList")
    public void givenNoStates_whenGetAllStates_thenReturnEmptyList() {
        // given
        given(carStateRepository.findAll()).willReturn(List.of());

        // when
        List<CarStateResponse> result = serviceUnderTest.getAllStates();

        // then
        assertThat(result).isEmpty();
        verify(carStateRepository).findAll();
        verifyNoInteractions(stateMapper);
    }

}
