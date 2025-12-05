package org.example.carshering.service.impl;

import org.example.carshering.dto.request.FilterCarModelRequest;
import org.example.carshering.dto.request.create.CreateCarModelRequest;
import org.example.carshering.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.domain.entity.Car;
import org.example.carshering.domain.entity.CarModel;
import org.example.carshering.exceptions.custom.AlreadyExistsException;
import org.example.carshering.exceptions.custom.EntityNotFoundException;
import org.example.carshering.exceptions.custom.InvalidQueryParameterException;
import org.example.carshering.mapper.ModelMapper;
import org.example.carshering.repository.CarModelRepository;
import org.example.carshering.service.domain.CarServiceHelperService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarModelServiceImplTests {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CarModelRepository carModelRepository;

    @Mock
    private CarServiceHelperService carService;

    @InjectMocks
    private CarModelServiceImpl serviceUnderTest;

    @Test
    @DisplayName("Test create model saves and returns response")
    public void givenCreateModelRequest_whenSave_thenReturnDto() {
        // given
        CreateCarModelRequest request = mock(CreateCarModelRequest.class);
        CarModel entity = new CarModel();
        CarModel saved = new CarModel();
        CarModelResponse response = new CarModelResponse(1L, "BMW", "X5", "SUV", "E", false);
        given(request.bodyType()).willReturn("SUV");
        given(request.brand()).willReturn("BMW");
        given(request.carClass()).willReturn("E");
        given(request.model()).willReturn("X5");

        given(modelMapper.toEntity(request)).willReturn(entity);
        given(carModelRepository.save(entity)).willReturn(saved);
        given(modelMapper.toDto(saved)).willReturn(response);
        given(carModelRepository.findByBodyTypeAndBrand_NameAndCarClass_NameAndModel_Name(
                eq("SUV"), eq("BMW"), eq("E"), eq("X5"))
        ).willReturn(Optional.empty());
        // when
        CarModelResponse actual = serviceUnderTest.createModel(request);

        // then
        assertThat(actual).isEqualTo(response);
        verify(carModelRepository).save(entity);
        verify(modelMapper).toDto(saved);
    }


    @Test
    @DisplayName("Test create model saves already exists model and returns exists exception")
    public void givenCreateModelRequestAlreadyExists_whenSave_thenExistsException() {
        // given
        CreateCarModelRequest request = mock(CreateCarModelRequest.class);
        given(request.bodyType()).willReturn("SUV");
        given(request.brand()).willReturn("BMW");
        given(request.carClass()).willReturn("E");
        given(request.model()).willReturn("X5");

        CarModel existing = new CarModel();
        given(carModelRepository.findByBodyTypeAndBrand_NameAndCarClass_NameAndModel_Name(
                eq("SUV"), eq("BMW"), eq("E"), eq("X5"))
        ).willReturn(Optional.of(existing));

        // when + then
        assertThrows(AlreadyExistsException.class, () -> serviceUnderTest.createModel(request));

        verify(carModelRepository, never()).save(any());
        verify(modelMapper, never()).toDto(any());
    }


    @Test
    @DisplayName("Test get model by id returns correct dto")
    public void givenExistingId_whenGetModelById_thenReturnDto() {
        // given
        CarModel model = new CarModel();
        model.setDeleted(false);
        CarModelResponse response = new CarModelResponse(1L, "BMW", "X5", "SUV", "E", false);

        given(carModelRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(model));
        given(modelMapper.toDto(model)).willReturn(response);

        // when
        CarModelResponse actual = serviceUnderTest.getModelById(1L);

        // then
        assertThat(actual).isEqualTo(response);
        verify(carModelRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    @DisplayName("Test get model by id throws exception if not found")
    public void givenInvalidId_whenGetModelById_thenThrowException() {
        // given
        given(carModelRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class, () -> serviceUnderTest.getModelById(1L));
    }

    @Test
    @DisplayName("Test delete model sets deleted=true and deletes cars")
    public void givenModelWithCars_whenDeleteModel_thenMarksDeletedAndDeletesCars() {
        // given
        CarModel model = new CarModel();
        model.setIdModel(1L);
        Car car1 = new Car();
        car1.setId(10L);
        Car car2 = new Car();
        car2.setId(11L);
        model.setCars(List.of(car1, car2));
        model.setDeleted(false);

        given(carModelRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(model));

        // when
        serviceUnderTest.deleteModel(1L);

        // then
        assertThat(model.isDeleted()).isTrue();
        verify(carService).deleteCar(10L);
        verify(carService).deleteCar(11L);
        verify(carModelRepository).save(model);
    }

    @Test
    @DisplayName("Test delete model throws exception when not found")
    public void givenInvalidId_whenDeleteModel_thenThrowException() {
        // given
        given(carModelRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class, () -> serviceUnderTest.deleteModel(1L));
        verify(carModelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test getAllModelsIncludingDeleted filters and maps correctly")
    public void givenFilterRequest_whenGetAllModelsIncludingDeleted_thenReturnsPageOfResponses() {
        // given
        FilterCarModelRequest request = new FilterCarModelRequest("BMW", "SUV", "E", false);
        Pageable pageable = mock(Pageable.class);
        Sort sort = Sort.by("brand");
        given(pageable.getSort()).willReturn(sort);

        CarModel model = new CarModel();
        CarModelResponse response = new CarModelResponse(1L, "BMW", "X5", "SUV", "E", false);
        Page<CarModel> page = new PageImpl<>(List.of(model));

        given(carModelRepository.findModelsByFilter(
                request.deleted(), request.brand(), request.bodyType(), request.carClass(), pageable))
                .willReturn(page);
        given(modelMapper.toDto(model)).willReturn(response);

        // when
        Page<CarModelResponse> result = serviceUnderTest.getAllModelsIncludingDeleted(request, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).brand()).isEqualTo("BMW");
        verify(carModelRepository).findModelsByFilter(false, "BMW", "SUV", "E", pageable);
    }

    @Test
    @DisplayName("Test validateSortProperties throws exception for invalid sort property")
    public void givenInvalidSortProperty_whenValidateSortProperties_thenThrowException() throws Exception {
        // given
        Sort sort = Sort.by("invalidProperty");

        var method = CarModelServiceImpl.class.getDeclaredMethod("validateSortProperties", Sort.class);
        method.setAccessible(true);

        // when + then
        assertThrows(InvocationTargetException.class, () -> method.invoke(serviceUnderTest, sort));
    }

    @Test
    @DisplayName("Test validateSortProperties allows valid properties")
    public void givenValidSortProperty_whenValidateSortProperties_thenNoException() throws Exception {
        // given
        Sort sort = Sort.by("brand");
        var method = CarModelServiceImpl.class.getDeclaredMethod("validateSortProperties", Sort.class);
        method.setAccessible(true);

        // when + then (не бросает исключений)
        method.invoke(serviceUnderTest, sort);
    }

    @Test
    @DisplayName("Test update model updates fields and returns response")
    public void givenValidUpdateRequest_whenUpdateModel_thenUpdateAndReturnDto() {
        // given
        CarModel existing = new CarModel();
        existing.setIdModel(1L);

        UpdateCarModelRequest request = mock(UpdateCarModelRequest.class);
        CarModelResponse response = new CarModelResponse(1L, "BMW", "X5", "SUV", "E", false);

        given(carModelRepository.findById(1L)).willReturn(Optional.of(existing));
        doNothing().when(modelMapper).updateCarFromDto(request, existing);
        given(carModelRepository.save(existing)).willReturn(existing);
        given(modelMapper.toDto(existing)).willReturn(response);

        // when
        CarModelResponse actual = serviceUnderTest.updateModel(1L, request);

        // then
        assertThat(actual).isEqualTo(response);
        verify(modelMapper).updateCarFromDto(request, existing);
        verify(carModelRepository).save(existing);
    }

    @Test
    @DisplayName("Test update model throws exception if model not found")
    public void givenInvalidId_whenUpdateModel_thenThrowException() {
        // given
        UpdateCarModelRequest request = mock(UpdateCarModelRequest.class);
        given(carModelRepository.findById(1L)).willReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class, () -> serviceUnderTest.updateModel(1L, request));
        verify(carModelRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test findAllBodyTypes returns distinct list from repository")
    public void whenFindAllBodyTypes_thenReturnList() {
        // given
        given(carModelRepository.findDistinctBodyTypes()).willReturn(List.of("SUV", "SEDAN"));

        // when
        List<String> result = serviceUnderTest.findAllBodyTypes();

        // then
        assertThat(result).containsExactly("SUV", "SEDAN");
        verify(carModelRepository).findDistinctBodyTypes();
    }

    @Test
    @DisplayName("Test getCarModelById returns entity if exists")
    public void givenExistingModelId_whenGetCarModelById_thenReturnEntity() {
        // given
        CarModel model = new CarModel();
        model.setIdModel(1L);
        given(carModelRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(model));

        // when
        CarModel actual = serviceUnderTest.getCarModelById(1L);

        // then
        assertThat(actual).isEqualTo(model);
    }

    @Test
    @DisplayName("Test getCarModelById throws exception if not found")
    public void givenInvalidModelId_whenGetCarModelById_thenThrowException() {
        // given
        given(carModelRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class, () -> serviceUnderTest.getCarModelById(1L));
    }


    @Test
    @DisplayName("Test getAllModelsIncludingDeleted throws exception for invalid sort property")
    public void givenInvalidSortInPageable_whenGetAllModelsIncludingDeleted_thenThrowException() {
        // given
        FilterCarModelRequest request = new FilterCarModelRequest(null, null, null, false);
        Pageable pageable = mock(Pageable.class);
        given(pageable.getSort()).willReturn(Sort.by("invalidField"));

        // when + then
        assertThrows(InvalidQueryParameterException.class,
                () -> serviceUnderTest.getAllModelsIncludingDeleted(request, pageable));
    }


    @Test
    @DisplayName("Test delete model sets deleted=true even if car list is empty")
    public void givenModelWithoutCars_whenDeleteModel_thenMarksDeleted() {
        // given
        CarModel model = new CarModel();
        model.setIdModel(1L);
        model.setCars(List.of()); // пустой список
        model.setDeleted(false);

        given(carModelRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(model));

        // when
        serviceUnderTest.deleteModel(1L);

        // then
        assertThat(model.isDeleted()).isTrue();
        verify(carService, never()).deleteCar(anyLong());
        verify(carModelRepository).save(model);
    }


    @Test
    @DisplayName("Test update model throws exception if mapper fails")
    public void givenMapperThrows_whenUpdateModel_thenPropagateException() {
        // given
        CarModel existing = new CarModel();
        given(carModelRepository.findById(1L)).willReturn(Optional.of(existing));

        UpdateCarModelRequest request = mock(UpdateCarModelRequest.class);
        doThrow(RuntimeException.class).when(modelMapper).updateCarFromDto(request, existing);

        // when + then
        assertThrows(RuntimeException.class, () -> serviceUnderTest.updateModel(1L, request));
    }

    @Test
    @DisplayName("Test validateSortProperties allows multiple valid properties")
    public void givenMultipleValidSortProperties_whenValidateSortProperties_thenNoException() throws Exception {
        // given
        Sort sort = Sort.by("brand", "model");
        var method = CarModelServiceImpl.class.getDeclaredMethod("validateSortProperties", Sort.class);
        method.setAccessible(true);

        // when + then
        method.invoke(serviceUnderTest, sort);
    }

}
