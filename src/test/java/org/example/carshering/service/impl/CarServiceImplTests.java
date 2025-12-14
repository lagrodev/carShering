//package org.example.carshering.service.impl;
//
//import org.example.carshering.dto.request.CarFilterRequest;
//import org.example.carshering.dto.request.create.CreateCarRequest;
//import org.example.carshering.dto.request.update.UpdateCarRequest;
//import org.example.carshering.fleet.api.dto.responce.CarDetailResponse;
//import org.example.carshering.fleet.api.dto.responce.CarListItemResponse;
//import org.example.carshering.fleet.api.dto.responce.CarStateResponse;
//import org.example.carshering.entity.*;
//import org.example.carshering.exceptions.custom.*;
//import org.example.carshering.mapper.CarMapper;
//import org.example.carshering.fleet.infrastructure.persistence.repository.CarRepository;
//import org.example.carshering.service.domain.CarModelHelperService;
//import org.example.carshering.service.domain.CarStateServiceHelper;
//import org.example.carshering.util.DataUtils;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//
//import java.lang.reflect.InvocationTargetException;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
//
//@ExtendWith(MockitoExtension.class)
//public class CarServiceImplTests {
//
//    @Mock
//    private CarRepository carRepository;
//    @Mock
//    private CarMapper carMapper;
//
//    @Mock
//    private CarStateServiceHelper carStateService;
//
//
//    @Mock
//    private CarModelHelperService carModelService;
//
//    @InjectMocks
//    private CarServiceImpl serviceUnderTest;
//
//
//    private DataUtils dataUtils = new DataUtils();
//
//
//    private List<?> getCarStateAndCarModelAndSaveAllDependencies(String state) {
//        Brand brand = dataUtils.getBrandTransient();
//
//        CarClass carClass = dataUtils.getCarClassTransient();
//
//        Model modelName = dataUtils.getModelNameTransient();
//
//        CarModel carModel = dataUtils.getCarModelSEDAN(brand, modelName, carClass);
//
//        CarStateType carState = dataUtils.getCarStateTransient(state);
//
//        return List.of(carState, carModel);
//    }
//
//
//    private List<?> getCarStateAndCarModelAndSaveAllDependencies(String state, Long idModel) {
//        Brand brand = dataUtils.getBrandTransient();
//
//        CarClass carClass = dataUtils.getCarClassTransient();
//
//        Model modelName = dataUtils.getModelNameTransient();
//
//        CarModel carModel = dataUtils.getCarModelSEDAN(brand, modelName, carClass);
//
//        CarStateType carState = dataUtils.getCarStatePersisted(state);
//
//        return List.of(carState, carModel);
//    }
//
//
//    @Test
//    @DisplayName("Test save car functionality")
//    public void givenCarToSave_whenSaveCar_thenRepositoryIsCalled() {
//        // given
//        CreateCarRequest request = dataUtils.createCarRequestTransient();
//
//        CarStateType state = DataUtils.getCarStateNEWSTATEPersisted("AVAILABLE");
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        Car carEntity = dataUtils.getJohnDoeTransient((CarStateType) list.get(0), (CarModel) list.get(1));
//
//
//        Car savedCar = dataUtils.getJohnDoePersisted((CarStateType) list.get(0), (CarModel) list.get(1));
//
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        given(carRepository.existsByGosNumber(request.gosNumber())).willReturn(false);
//        given(carRepository.existsByVin(request.vin())).willReturn(false);
//
//        given(carMapper.toEntity(request)).willReturn(carEntity);
//
//        given(carStateService.getStateByName("AVAILABLE"))
//                .willReturn(state);
//
//        given(carRepository.save(carEntity)).willReturn(savedCar);
//
//        given(carMapper.toDetailDto(savedCar, false)).willReturn(response);
//
//        // when
//
//        CarDetailResponse actual = serviceUnderTest.createCar(request);
//
//        // then
//
//        assertThat(actual).isNotNull();
//        assertThat(actual.vin()).isEqualTo("JOHNDOE");
//        assertThat(actual.gosNumber()).isEqualTo("1123");
//        assertThat(actual.status()).isEqualTo("AVAILABLE");
//
//        verify(carMapper).toEntity(request);
//        verify(carRepository).save(carEntity);
//        verify(carMapper).toDetailDto(savedCar, false);
//    }
//
//    @Test
//    @DisplayName("Test save car with duplicate vin functionality")
//    public void givenCarToSaveWithDuplicateVin_whenSaveCar_thenThrowException() {
//        // given
//        CreateCarRequest request = dataUtils.createCarRequestTransient();
//
//        given(carRepository.existsByVin(request.vin()))
//                .willReturn(true);
//
//        // when
//        assertThrows(
//                AlreadyExistsException.class,
//                () -> serviceUnderTest.createCar(request)
//        );
//
//        // then
//        verify(carRepository, never()).save(any(Car.class));
//    }
//
//    @Test
//    @DisplayName("Test save car with duplicate gosNumber functionality")
//    public void givenCarToSaveWithDuplicateGosNumber_whenSaveCar_thenThrowException() {
//        // given
//        CreateCarRequest request = dataUtils.createCarRequestTransient();
//
//        given(carRepository.existsByGosNumber(request.gosNumber()))
//                .willReturn(true);
//
//        // when
//        assertThrows(
//                AlreadyExistsException.class,
//                () -> serviceUnderTest.createCar(request)
//        );
//
//        // then
//        verify(carRepository, never()).save(any(Car.class));
//    }
//
//    @Test
//    @DisplayName("Test save car with missing state throws exception")
//    public void givenNoCarState_whenCreateCar_thenThrowException() {
//        // given
//        CreateCarRequest request = dataUtils.createCarRequestTransient();
//
//        given(carRepository.existsByVin(request.vin())).willReturn(false);
//        given(carRepository.existsByGosNumber(request.gosNumber())).willReturn(false);
//
//        given(carStateService.getStateByName("AVAILABLE"))
//                .willThrow(new NotFoundException("State not found"));
//
//        // when + then
//        assertThrows(
//                NotFoundException.class,
//                () -> serviceUnderTest.createCar(request)
//        );
//
//        verify(carRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("Test update car without changing VIN")
//    public void givenCarToUpdateWithoutChangedVINAndGosNomer_whenUpdateCar_thenRepositoryIsCalled() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//
//        UpdateCarRequest request = dataUtils.updateCarRequestTransient();
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoePersisted(state, model);
//        Car savedCar = dataUtils.getJohnDoePersisted(state, model);
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//        given(carRepository.save(any(Car.class))).willReturn(savedCar);
//        given(carMapper.toDetailDto(savedCar)).willReturn(response);
//        given(carModelService.getCarModelById(request.modelId())).willReturn(model);
//
//        // when
//        CarDetailResponse actual = serviceUnderTest.updateCar(1L, request);
//
//        // then
//        assertThat(actual).isNotNull();
//
//        verify(carMapper).updateCar(existingCar, request);
//        verify(carRepository).findById(1L);
//        verify(carRepository, never()).existsByVin(anyString());
//        verify(carRepository, never()).existsByGosNumber(anyString());
//        verify(carRepository).save(existingCar);
//        verify(carMapper).toDetailDto(savedCar);
//    }
//
//    @Test
//    @DisplayName("Test update car with changed VIN and without changing GosNomer")
//    public void givenCarToUpdateWithChangedVinButSameGosNomer_whenUpdateCar_thenChecksVinUniquenessAndSaves() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//
//        UpdateCarRequest request = dataUtils.updateCarRequestTransient();
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getMikeSmithPersisted(state, model);
//
//        Car savedCar = dataUtils.getJohnDoePersisted(state, model);
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//        given(carRepository.existsByVin(request.vin())).willReturn(false);
//        given(carRepository.save(any(Car.class))).willReturn(savedCar);
//        given(carMapper.toDetailDto(savedCar)).willReturn(response);
//        given(carModelService.getCarModelById(request.modelId())).willReturn(model);
//
//        // when
//        CarDetailResponse actual = serviceUnderTest.updateCar(1L, request);
//
//        // then
//        assertThat(actual).isNotNull();
//
//        verify(carMapper).updateCar(existingCar, request);
//        verify(carRepository).findById(1L);
//        verify(carRepository).existsByVin(request.vin());
//        verify(carRepository, never()).existsByGosNumber(anyString());
//        verify(carRepository).save(existingCar);
//        verify(carMapper).toDetailDto(savedCar);
//    }
//
//    @Test
//    @DisplayName("Test update car with changed GosNomer and without changing VIN")
//    public void updateCarWithChangedGosNomerAndWithoutChangedVin_whenUpdateCar_thenRepositoryIsCalled() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//
//        UpdateCarRequest request = dataUtils.updateCarRequestTransient();
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getFrankJonesPersisted(state, model);
//
//        Car savedCar = dataUtils.getJohnDoePersisted(state, model);
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//        given(carRepository.existsByGosNumber(request.gosNumber())).willReturn(false);
//        given(carRepository.save(any(Car.class))).willReturn(savedCar);
//        given(carMapper.toDetailDto(savedCar)).willReturn(response);
//        given(carModelService.getCarModelById(request.modelId())).willReturn(model);
//
//        // when
//        CarDetailResponse actual = serviceUnderTest.updateCar(1L, request);
//
//        // then
//        assertThat(actual).isNotNull();
//
//        verify(carMapper).updateCar(existingCar, request);
//        verify(carRepository).findById(1L);
//        verify(carRepository, never()).existsByVin(anyString());
//        verify(carRepository).existsByGosNumber(request.gosNumber());
//        verify(carRepository).save(existingCar);
//        verify(carMapper).toDetailDto(savedCar);
//    }
//
//    @Test
//    @DisplayName("Test update car with changed VIN and changed GosNomer")
//    public void updateCarWithChangedVinAndChangedGosNomer_whenUpdateCar_thenRepositoryIsCalled() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//
//        UpdateCarRequest request = dataUtils.updateCarRequestNeverUsedTransient();
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getFrankJonesPersisted(state, model);
//
//        Car savedCar = dataUtils.getNeverUsedTransient(state, model);
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//        given(carRepository.existsByVin(request.vin())).willReturn(false);
//        given(carRepository.existsByGosNumber(request.gosNumber())).willReturn(false);
//        given(carRepository.save(any(Car.class))).willReturn(savedCar);
//        given(carMapper.toDetailDto(savedCar)).willReturn(response);
//        given(carModelService.getCarModelById(request.modelId())).willReturn(model);
//
//        // when
//        CarDetailResponse actual = serviceUnderTest.updateCar(1L, request);
//
//        // then
//        assertThat(actual).isNotNull();
//
//        verify(carMapper).updateCar(existingCar, request);
//        verify(carRepository).findById(1L);
//        verify(carRepository).existsByVin(request.vin());
//        verify(carRepository).existsByGosNumber(request.gosNumber());
//        verify(carRepository).save(existingCar);
//        verify(carMapper).toDetailDto(savedCar);
//    }
//
//    @Test
//    @DisplayName("Test update car with incorrect id throws exception")
//    public void givenCarToUpdateIncorrectId_whenUpdateCar_thenThrowException() {
//        // given
//        UpdateCarRequest request = dataUtils.updateCarRequestTransient();
//
//        given(carRepository.findById(anyLong())).willReturn(Optional.empty());
//
//        // when + then
//        assertThrows(
//                CarNotFoundException.class,
//                () -> serviceUnderTest.updateCar(1L, request)
//        );
//
//        verify(carRepository, never()).save(any());
//        verify(carMapper, never()).updateCar(any(), any());
//        verify(carModelService, never()).getCarModelById(any());
//
//    }
//
//    @Test
//    @DisplayName("Test update car with duplicate vin throws exception")
//    public void updateCarWithDuplicateVin_whenUpdateCar_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//
//        UpdateCarRequest request = dataUtils.updateCarRequestNeverUsedTransient();
//
//
//        given(carRepository.existsByVin(request.vin())).willReturn(true);
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//        Car existingCar = dataUtils.getFrankJonesPersisted(state, model);
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//
//        // when
//        assertThrows(
//                AlreadyExistsException.class,
//                () -> serviceUnderTest.updateCar(1L, request)
//        );
//
//        // then
//
//        verify(carRepository, never()).save(any());
//        verify(carMapper, never()).updateCar(any(), any());
//        verify(carModelService, never()).getCarModelById(any());
//
//    }
//
//    @Test
//    @DisplayName("Test update car with duplicate gosNomer throws exception")
//    public void updateCarWithDuplicateGosNomer_whenUpdateCar_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//
//        UpdateCarRequest request = dataUtils.updateCarRequestNeverUsedTransient();
//
//        given(carRepository.existsByGosNumber(request.gosNumber())).willReturn(true);
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//        Car existingCar = dataUtils.getFrankJonesPersisted(state, model);
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//        // when
//        assertThrows(
//                AlreadyExistsException.class,
//                () -> serviceUnderTest.updateCar(1L, request)
//        );
//        // then
//        verify(carRepository, never()).save(any());
//        verify(carMapper, never()).updateCar(any(), any());
//        verify(carModelService, never()).getCarModelById(any());
//    }
//
//    @Test
//    @DisplayName("Test update car with non-existing car model throws exception")
//    public void updateCar_whenCarModelNotFound_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//
//        UpdateCarRequest request = dataUtils.updateCarRequestNeverUsedTransient();
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getFrankJonesPersisted(state, model);
//
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//        given(carRepository.existsByVin(request.vin())).willReturn(false);
//        given(carRepository.existsByGosNumber(request.gosNumber())).willReturn(false);
//
//
//        given(carModelService.getCarModelById(anyLong()))
//                .willThrow(new NotFoundException("CarModel not found"));
//
//        // when + then
//        assertThrows(NotFoundException.class, () -> serviceUnderTest.updateCar(1L, request));
//
//        verify(carRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("Test update car updates model correctly")
//    public void givenNewModel_whenUpdateCar_thenModelIsChangedAndSaved() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel oldModel = (CarModel) list.get(1);
//        Car existingCar = dataUtils.getFrankJonesTransient(state, oldModel);
//        Car carSpy = spy(existingCar);
//
//        UpdateCarRequest request = dataUtils.updateCarRequestTransient();
//        CarModel newModel = dataUtils.getCarModelBody(
//                dataUtils.getBrandTransient("BMW"),
//                dataUtils.getModelNameTransient(),
//                dataUtils.getCarClassTransient(),
//                "Hatchback"
//        );
//        newModel.setIdModel(1L);
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(carSpy));
//        given(carModelService.getCarModelById(newModel.getIdModel())).willReturn(newModel);
//        given(carRepository.save(any(Car.class))).willReturn(carSpy);
//        given(carMapper.toDetailDto(carSpy)).willReturn(DataUtils.carDetailResponsePersisted());
//
//        // when
//        serviceUnderTest.updateCar(1L, request);
//
//        // then
//        verify(carSpy).setModel(same(newModel));
//        verify(carRepository).save(carSpy);
//        verify(carMapper).toDetailDto(carSpy);
//    }
//
//    @Test
//    @DisplayName("Test update car without modelId skips model update")
//    public void givenUpdateRequestWithoutModelId_whenUpdateCar_thenModelIsNotChangedAndCarIsSaved() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoePersisted(state, model);
//        Car savedCar = dataUtils.getJohnDoePersisted(state, model);
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        UpdateCarRequest request = dataUtils.updateCarRequestWithoutModelId();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//        given(carRepository.save(existingCar)).willReturn(savedCar);
//        given(carMapper.toDetailDto(savedCar)).willReturn(response);
//
//        // when
//        CarDetailResponse actual = serviceUnderTest.updateCar(1L, request);
//
//        // then
//        assertThat(actual).isNotNull();
//
//        verify(carMapper).updateCar(existingCar, request);
//        verify(carRepository).findById(1L);
//        verify(carRepository, never()).existsByVin(anyString());
//        verify(carRepository, never()).existsByGosNumber(anyString());
//        verify(carModelService, never()).getCarModelById(anyLong());
//        verify(carRepository).save(existingCar);
//        verify(carMapper).toDetailDto(savedCar);
//    }
//
//    @Test
//    @DisplayName("Test update car with null modelId and changed VIN")
//    public void givenUpdateRequestWithoutModelIdAndChangedVin_whenUpdateCar_thenChecksVinUniquenessAndSaves() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getMikeSmithPersisted(state, model);
//        Car savedCar = dataUtils.getJohnDoePersisted(state, model);
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        UpdateCarRequest request = dataUtils.updateCarRequestWithoutModelId();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//        given(carRepository.existsByVin(request.vin())).willReturn(false);
//        given(carRepository.save(existingCar)).willReturn(savedCar);
//        given(carMapper.toDetailDto(savedCar)).willReturn(response);
//
//        // when
//        CarDetailResponse actual = serviceUnderTest.updateCar(1L, request);
//
//        // then
//        assertThat(actual).isNotNull();
//
//        verify(carMapper).updateCar(existingCar, request);
//        verify(carRepository).findById(1L);
//        verify(carRepository).existsByVin(request.vin());
//        verify(carRepository, never()).existsByGosNumber(anyString());
//        verify(carModelService, never()).getCarModelById(anyLong());
//        verify(carRepository).save(existingCar);
//        verify(carMapper).toDetailDto(savedCar);
//    }
//
//    @Test
//    @DisplayName("Test update car with null modelId and changed GosNomer")
//    public void givenUpdateRequestWithoutModelIdAndChangedGosNomer_whenUpdateCar_thenChecksGosNumberUniquenessAndSaves() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getFrankJonesPersisted(state, model);
//        Car savedCar = dataUtils.getJohnDoePersisted(state, model);
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        UpdateCarRequest request = dataUtils.updateCarRequestWithoutModelId();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//        given(carRepository.existsByGosNumber(request.gosNumber())).willReturn(false);
//        given(carRepository.save(existingCar)).willReturn(savedCar);
//        given(carMapper.toDetailDto(savedCar)).willReturn(response);
//
//        // when
//        CarDetailResponse actual = serviceUnderTest.updateCar(1L, request);
//
//        // then
//        assertThat(actual).isNotNull();
//
//        verify(carMapper).updateCar(existingCar, request);
//        verify(carRepository).findById(1L);
//        verify(carRepository, never()).existsByVin(anyString());
//        verify(carRepository).existsByGosNumber(request.gosNumber());
//        verify(carModelService, never()).getCarModelById(anyLong());
//        verify(carRepository).save(existingCar);
//        verify(carMapper).toDetailDto(savedCar);
//    }
//
//    @Test
//    @DisplayName("Test update car with duplicate VIN and same GosNomer throws exception")
//    public void givenUpdateRequestWithDuplicateVinAndSameGosNomer_whenUpdateCar_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getMikeSmithPersisted(state, model);
//        UpdateCarRequest request = dataUtils.updateCarRequestTransient();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//        given(carRepository.existsByVin(request.vin())).willReturn(true);
//
//        // when + then
//        assertThrows(
//                AlreadyExistsException.class,
//                () -> serviceUnderTest.updateCar(1L, request)
//        );
//
//        verify(carRepository).findById(1L);
//        verify(carRepository, never()).existsByGosNumber(anyString());
//        verify(carRepository, never()).save(any());
//        verify(carMapper, never()).updateCar(any(), any());
//        verify(carModelService, never()).getCarModelById(any());
//    }
//
//    @Test
//    @DisplayName("Test update car with same VIN and duplicate GosNomer throws exception")
//    public void givenUpdateRequestWithSameVinAndDuplicateGosNomer_whenUpdateCar_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getFrankJonesPersisted(state, model);
//        UpdateCarRequest request = dataUtils.updateCarRequestTransient();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//        given(carRepository.existsByGosNumber(request.gosNumber())).willReturn(true);
//
//        // when + then
//        assertThrows(
//                AlreadyExistsException.class,
//                () -> serviceUnderTest.updateCar(1L, request)
//        );
//
//        verify(carRepository).findById(1L);
//        verify(carRepository, never()).existsByVin(anyString());
//        verify(carRepository, never()).save(any());
//        verify(carMapper, never()).updateCar(any(), any());
//        verify(carModelService, never()).getCarModelById(any());
//    }
//
//    @Test
//    @DisplayName("Test update car mapper is invoked before save")
//    public void givenValidUpdateRequest_whenUpdateCar_thenMapperIsInvokedBeforeSave() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoePersisted(state, model);
//        Car savedCar = dataUtils.getJohnDoePersisted(state, model);
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        UpdateCarRequest request = dataUtils.updateCarRequestTransient();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//        given(carModelService.getCarModelById(request.modelId())).willReturn(model);
//        given(carRepository.save(existingCar)).willReturn(savedCar);
//        given(carMapper.toDetailDto(savedCar)).willReturn(response);
//
//        // when
//        serviceUnderTest.updateCar(1L, request);
//
//        // then
//        verify(carMapper).updateCar(existingCar, request);
//        verify(carRepository).save(existingCar);
//    }
//
//    @Test
//    @DisplayName("Test update car with blank VIN throws exception")
//    public void givenUpdateRequestWithBlankVin_whenUpdateCar_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoePersisted(state, model);
//        UpdateCarRequest request = dataUtils.updateCarRequestWithBlankVin();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//
//        // when + then
//        assertThrows(
//                InvalidDataException.class,
//                () -> serviceUnderTest.updateCar(1L, request)
//        );
//
//        verify(carRepository).findById(1L);
//
//        verify(carRepository, never()).save(any());
//        verify(carMapper, never()).updateCar(any(), any());
//        verify(carModelService, never()).getCarModelById(any());
//    }
//
//    @Test
//    @DisplayName("Test update car with empty VIN throws exception")
//    public void givenUpdateRequestWithEmptyVin_whenUpdateCar_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoePersisted(state, model);
//        UpdateCarRequest request = dataUtils.updateCarRequestWithEmptyVin();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//
//        // when + then
//        assertThrows(
//                InvalidDataException.class,
//                () -> serviceUnderTest.updateCar(1L, request)
//        );
//
//        verify(carRepository).findById(1L);
//
//        verify(carRepository, never()).save(any());
//        verify(carMapper, never()).updateCar(any(), any());
//        verify(carModelService, never()).getCarModelById(any());
//    }
//
//    @Test
//    @DisplayName("Test update car with blank GosNomer throws exception")
//    public void givenUpdateRequestWithBlankGosNomer_whenUpdateCar_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoePersisted(state, model);
//        UpdateCarRequest request = dataUtils.updateCarRequestWithBlankGosNumber();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//
//        // when + then
//        assertThrows(
//                InvalidDataException.class,
//                () -> serviceUnderTest.updateCar(1L, request)
//        );
//
//        verify(carRepository).findById(1L);
//
//        verify(carRepository, never()).save(any());
//        verify(carMapper, never()).updateCar(any(), any());
//        verify(carModelService, never()).getCarModelById(any());
//    }
//
//    @Test
//    @DisplayName("Test update car with empty GosNomer throws exception")
//    public void givenUpdateRequestWithEmptyGosNomer_whenUpdateCar_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoePersisted(state, model);
//        UpdateCarRequest request = dataUtils.updateCarRequestWithEmptyGosNumber();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//
//        // when + then
//        assertThrows(
//                InvalidDataException.class,
//                () -> serviceUnderTest.updateCar(1L, request)
//        );
//
//        verify(carRepository).findById(1L);
//
//        verify(carRepository, never()).save(any());
//        verify(carMapper, never()).updateCar(any(), any());
//        verify(carModelService, never()).getCarModelById(any());
//    }
//
//    @Test
//    @DisplayName("Test update car with null VIN skips VIN uniqueness check")
//    public void givenUpdateRequestWithNullVin_whenUpdateCar_thenSkipsVinCheckAndUpdatesSuccessfully() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoePersisted(state, model);
//        Car savedCar = dataUtils.getJohnDoePersisted(state, model);
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        UpdateCarRequest request = dataUtils.updateCarRequestWithNullVin();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//        given(carRepository.existsByVin(null)).willReturn(false);
//        given(carModelService.getCarModelById(request.modelId())).willReturn(model);
//        given(carRepository.save(existingCar)).willReturn(savedCar);
//        given(carMapper.toDetailDto(savedCar)).willReturn(response);
//
//        // when
//        CarDetailResponse actual = serviceUnderTest.updateCar(1L, request);
//
//        // then
//        assertThat(actual).isNotNull();
//
//        verify(carMapper).updateCar(existingCar, request);
//        verify(carRepository).findById(1L);
//        verify(carRepository).existsByVin(null);
//        verify(carRepository, never()).existsByGosNumber(anyString());
//        verify(carModelService).getCarModelById(request.modelId());
//        verify(carRepository).save(existingCar);
//        verify(carMapper).toDetailDto(savedCar);
//    }
//
//
//    @Test
//    @DisplayName("Test update car with blank VIN and blank GosNomer throws exception")
//    public void givenUpdateRequestWithBlankVinAndBlankGosNomer_whenUpdateCar_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoePersisted(state, model);
//        UpdateCarRequest request = dataUtils.updateCarRequestWithBlankVinAndGosNumber();
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(existingCar));
//
//        // when + then
//        assertThrows(
//                InvalidDataException.class,
//                () -> serviceUnderTest.updateCar(1L, request)
//        );
//
//        verify(carRepository).findById(1L);
//        verify(carRepository, never()).save(any());
//        verify(carMapper, never()).updateCar(any(), any());
//        verify(carModelService, never()).getCarModelById(any());
//    }
//
//    @Test
//    @DisplayName("Test get car by id functionality")
//    public void givenId_whenGetById_thenCarIsReturned() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoeTransient(state, model);
//
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//        given(carMapper.toDetailDto(existingCar)).willReturn(response);
//
//        // when
//        CarDetailResponse actual = serviceUnderTest.findById(1L);
//
//        // then
//        assertThat(actual).isNotNull();
//        assertThat(actual.vin()).isEqualTo("JOHNDOE");
//        assertThat(actual.gosNumber()).isEqualTo("1123");
//        assertThat(actual.status()).isEqualTo("AVAILABLE");
//
//        verify(carRepository).findById(1L);
//        verify(carMapper).toDetailDto(existingCar);
//    }
//
//
//    @Test
//    @DisplayName("Test get car by id functionality")
//    public void givenIncorrectId_whenGetById_thenExceptionThrow() {
//        // given
//        given(carRepository.findById(anyLong())).willThrow(
//                new CarNotFoundException("Car not found with id: 1")
//        );
//
//        assertThrows(CarNotFoundException.class, () -> serviceUnderTest.findById(1L));
//
//        // then
//        verify(carMapper, never()).toDetailDto(any(Car.class));
//    }
//
//    @Test
//    @DisplayName("Test get car entity by id functionality")
//    public void getCarEntityById_whenGetEntity_thenCarIsReturned() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoeTransient(state, model);
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//
//        // when
//        Car actual = serviceUnderTest.getEntity(1L);
//        // then
//        assertThat(actual).isNotNull();
//        assertThat(actual.getVin()).isEqualTo("JOHNDOE");
//        assertThat(actual.getGosNumber()).isEqualTo("1123");
//        assertThat(actual.getState().getStatus()).isEqualTo("AVAILABLE");
//
//        verify(carRepository).findById(1L);
//
//    }
//
//    @Test
//    @DisplayName("Test get incorrect car entity by id functionality")
//    public void getIncorrectCarEntityById_whenGetEntity_thenExceptionThrow() {
//        // given
//        given(carRepository.findById(anyLong())).willThrow(
//                new CarNotFoundException("Car not found with id: 1")
//        );
//
//        assertThrows(CarNotFoundException.class, () -> serviceUnderTest.getEntity(1L));
//
//        // then
//        verify(carRepository).findById(1L);
//    }
//
//
//    @Test
//    @DisplayName("Test get valid car by id functionality")
//    public void getValidCarById_whenGetValidCarById_thenCarIsReturned() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoeTransient(state, model);
//
//        CarDetailResponse response = DataUtils.carDetailResponsePersisted();
//
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//        given(carMapper.toDetailDto(existingCar, false)).willReturn(response);
//
//        // when
//        CarDetailResponse actual = serviceUnderTest.getValidCarById(1L, false);
//
//        // then
//        assertThat(actual).isNotNull();
//        assertThat(actual.vin()).isEqualTo("JOHNDOE");
//        assertThat(actual.gosNumber()).isEqualTo("1123");
//        assertThat(actual.status()).isEqualTo("AVAILABLE");
//
//        verify(carRepository).findById(1L);
//        verify(carMapper).toDetailDto(existingCar, false);
//    }
//
//
//    @Test
//    @DisplayName("Test get invalid car by id functionality")
//    public void getInvalidCarById_whenGetValidCarById_thenExceptionThrow() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("BOOKED");
//
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getJohnDoeTransient(state, model);
//
//        given(carRepository.findById(anyLong())).willReturn(Optional.of(existingCar));
//
//        // when + then
//        assertThrows(
//                StateException.class,
//                () -> serviceUnderTest.getValidCarById(1L, false)
//        );
//
//        verify(carRepository).findById(1L);
//        verify(carMapper, never()).toDetailDto(any(Car.class), anyBoolean());
//    }
//
//    @Test
//    @DisplayName("Test get non-existing car by id functionality")
//    public void getInvalidCarById_whenGetValidCarById_thenExceptionThrow_CarNotFound() {
//        // given
//        given(carRepository.findById(anyLong())).willThrow(
//                new CarNotFoundException("Car not found with id: 1")
//        );
//
//        // when + then
//        assertThrows(
//                CarNotFoundException.class,
//                () -> serviceUnderTest.getValidCarById(1L, false)
//        );
//
//        verify(carRepository).findById(1L);
//        verify(carMapper, never()).toDetailDto(any(Car.class), anyBoolean());
//    }
//
//    @Test
//    @DisplayName("Test update car state functionality")
//    public void updateCarState_whenUpdateCarState_thenStateIsChangedAndSaved() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//
//        CarModel model = (CarModel) list.get(1);
//        model.setDeleted(true);
//
//        Car existingCar = dataUtils.getFrankJonesTransient(state, model);
//
//        Car carSpy = spy(existingCar);
//
//        CarStateType newState = dataUtils.getCarStatePersisted("RENTED");
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(carSpy));
//        given(carStateService.getStateByName("RENTED")).willReturn((newState));
//        given(carRepository.save(any(Car.class))).willReturn(carSpy);
//
//        // when
//        CarStateResponse carStateResponse = serviceUnderTest.updateCarState(1L, "RENTED");
//
//        // then
//        verify(carSpy).setState(same(newState));
//
//        assertThat(carStateResponse.id()).isEqualTo(1L);
//        assertThat(carStateResponse.status()).isEqualTo("RENTED");
//
//        verify(carRepository).save(carSpy);
//    }
//
//    @Test
//    @DisplayName("Test update car state with incorrect state throws exception")
//    public void updateCarStateIncorrectState_whenUpdateCarState_thenThrowException() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType state = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//
//        Car existingCar = dataUtils.getFrankJonesTransient(state, model);
//
//        Car carSpy = spy(existingCar);
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(carSpy));
//        given(carStateService.getStateByName("NON_EXISTING_STATE"))
//                .willThrow(new NotFoundException("State not found"));
//
//        // when + then
//        assertThrows(
//                NotFoundException.class,
//                () -> serviceUnderTest.updateCarState(1L, "NON_EXISTING_STATE")
//        );
//
//        verify(carRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("Test update car state with incorrect id throws exception")
//    public void updateCarStateIncorrectId_whenUpdateCarState_thenThrowException() {
//        // given
//        given(carRepository.findById(1L)).willReturn(Optional.empty());
//
//        // when + then
//        assertThrows(
//                CarNotFoundException.class,
//                () -> serviceUnderTest.updateCarState(1L, "RENTED")
//        );
//
//        verify(carRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("Test update car state sets model deleted=false when model is deleted and state not UNAVAILABLE")
//    public void updateCarState_whenModelDeletedAndNewStateNotUnavailable_thenModelSetDeletedFalse() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("UNAVAILABLE");
//        CarStateType oldState = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//        model.setDeleted(true);
//
//        Car existingCar = dataUtils.getFrankJonesTransient(oldState, model);
//        Car carSpy = spy(existingCar);
//
//        CarStateType newState = dataUtils.getCarStatePersisted("AVAILABLE");
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(carSpy));
//        given(carStateService.getStateByName("AVAILABLE")).willReturn(newState);
//        given(carRepository.save(any(Car.class))).willReturn(carSpy);
//
//        // when
//        CarStateResponse carStateResponse = serviceUnderTest.updateCarState(1L, "AVAILABLE");
//
//        // then
//        assertThat(carStateResponse.status()).isEqualTo("AVAILABLE");
//        assertThat(carStateResponse.id()).isEqualTo(1L);
//
//        verify(carSpy).setState(same(newState));
//        verify(carRepository).save(carSpy);
//        assertThat(model.isDeleted()).isFalse();
//    }
//
//    @Test
//    @DisplayName("Test update car state keeps model deleted=true when new state is UNAVAILABLE")
//    public void updateCarState_whenModelDeletedAndNewStateUnavailable_thenModelDeletedStaysTrue() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("RENTED");
//        CarStateType oldState = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//        model.setDeleted(true);
//
//        Car existingCar = dataUtils.getFrankJonesTransient(oldState, model);
//        Car carSpy = spy(existingCar);
//
//        CarStateType newState = dataUtils.getCarStatePersisted("UNAVAILABLE");
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(carSpy));
//        given(carStateService.getStateByName("UNAVAILABLE")).willReturn(newState);
//        given(carRepository.save(any(Car.class))).willReturn(carSpy);
//
//        // when
//        CarStateResponse carStateResponse =  serviceUnderTest.updateCarState(1L, "UNAVAILABLE");
//
//        // then
//        assertThat(carStateResponse.status()).isEqualTo("UNAVAILABLE");
//        assertThat(carStateResponse.id()).isEqualTo(1L);
//        verify(carSpy).setState(same(newState));
//        verify(carRepository).save(carSpy);
//        assertThat(model.isDeleted()).isTrue();
//    }
//    @Test
//    @DisplayName("Test update car state changes only state when model not deleted")
//    public void updateCarState_whenModelNotDeleted_thenOnlyStateChanges() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType oldState = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//        model.setDeleted(false);
//
//        Car existingCar = dataUtils.getFrankJonesTransient(oldState, model);
//        Car carSpy = spy(existingCar);
//
//        CarStateType newState = dataUtils.getCarStatePersisted("BOOKED");
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(carSpy));
//        given(carStateService.getStateByName("BOOKED")).willReturn(newState);
//        given(carRepository.save(any(Car.class))).willReturn(carSpy);
//
//        // when
//        CarStateResponse carStateResponse =  serviceUnderTest.updateCarState(1L, "BOOKED");
//
//        // then
//        assertThat(carStateResponse.status()).isEqualTo("BOOKED");
//        assertThat(carStateResponse.id()).isEqualTo(1L);
//        verify(carSpy).setState(same(newState));
//        verify(carRepository).save(carSpy);
//        assertThat(model.isDeleted()).isFalse();
//    }
//
//    @Test
//    @DisplayName("Test delete car calls updateCarState with UNAVAILABLE state")
//    public void deleteCar_whenDeleteCar_thenUpdateCarStateCalledWithUnavailable() {
//        // given
//        CarServiceImpl spyService = spy(serviceUnderTest);
//
//        CarStateResponse response = new CarStateResponse(1L, "UNAVAILABLE");
//
//        doReturn(response).when(spyService).updateCarState(1L, "UNAVAILABLE");
//
//        // when
//        spyService.deleteCar(1L);
//
//        // then
//        verify(spyService).updateCarState(1L, "UNAVAILABLE");
//    }
//
//    @Test
//    @DisplayName("Test delete car sets state to UNAVAILABLE and saves it")
//    public void deleteCar_whenDeleteCar_thenStateIsUnavailableAndSaved() {
//        // given
//        var list = getCarStateAndCarModelAndSaveAllDependencies("AVAILABLE");
//        CarStateType availableState = (CarStateType) list.get(0);
//        CarModel model = (CarModel) list.get(1);
//        model.setDeleted(true); // имитируем удалённую модель, чтобы проверить что она не восстановится
//
//        Car existingCar = dataUtils.getFrankJonesTransient(availableState, model);
//        Car carSpy = spy(existingCar);
//
//        CarStateType unavailableState = dataUtils.getCarStateTransient("UNAVAILABLE");
//
//        given(carRepository.findById(1L)).willReturn(Optional.of(carSpy));
//        given(carStateService.getStateByName("UNAVAILABLE")).willReturn(unavailableState);
//        given(carRepository.save(any(Car.class))).willReturn(carSpy);
//
//        // when
//        serviceUnderTest.deleteCar(1L);
//
//        // then
//        verify(carSpy).setState(same(unavailableState));
//        verify(carRepository).save(carSpy);
//
//        // проверяем, что модель осталась удалённой (deleted = true)
//        assertThat(carSpy.getModel().isDeleted()).isTrue();
//    }
//
//
//    @Test
//    @DisplayName("Test delete car with incorrect id throws exception")
//    public void deleteCarIncorrectId_whenDeleteCar_thenThrowException() {
//        // given
//        given(carRepository.findById(1L)).willReturn(Optional.empty());
//
//        // when + then
//        assertThrows(
//                CarNotFoundException.class,
//                () -> serviceUnderTest.deleteCar(1L)
//        );
//
//        verify(carRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("Test isEmpty returns true for null or empty list")
//    public void givenNullOrEmptyList_whenIsEmpty_thenReturnTrue() throws Exception {
//        // given
//        var method = CarServiceImpl.class.getDeclaredMethod("isEmpty", List.class);
//        method.setAccessible(true);
//
//        // when
//        boolean resultNull = (boolean) method.invoke(serviceUnderTest, (Object) null);
//        boolean resultEmpty = (boolean) method.invoke(serviceUnderTest, List.of());
//
//        // then
//        assertThat(resultNull).isTrue();
//        assertThat(resultEmpty).isTrue();
//    }
//
//
//    @Test
//    @DisplayName("Test isEmpty returns false for non-empty list")
//    public void givenNonEmptyList_whenIsEmpty_thenReturnFalse() throws Exception {
//        // given
//        var method = CarServiceImpl.class.getDeclaredMethod("isEmpty", List.class);
//        method.setAccessible(true);
//
//        // when
//        boolean result = (boolean) method.invoke(serviceUnderTest, List.of("A", "B"));
//
//        // then
//        assertThat(result).isFalse();
//    }
//
//
//    @Test
//    @DisplayName("Test validateSortProperties throws exception for invalid property")
//    public void givenInvalidSortProperty_whenValidateSortProperties_thenThrowException() throws Exception {
//        // given
//        Sort sort = Sort.by("unknownProperty");
//
//        var method = CarServiceImpl.class.getDeclaredMethod("validateSortProperties", Sort.class);
//        method.setAccessible(true);
//
//        // then
//        assertThrows(
//                InvocationTargetException.class,
//                () -> method.invoke(serviceUnderTest, sort)
//        );
//    }
//
//
//    @Test
//    @DisplayName("Test getCarOrThrow throws exception for invalid property")
//    public void givenInvalidCarToGet_whenCarExists_thenThrowException()  throws Exception  {
//        // given
//        var method = CarServiceImpl.class.getDeclaredMethod("getCarOrThrow", Long.class);
//        method.setAccessible(true);
//
//        assertThrows(
//                InvocationTargetException.class,
//                () -> method.invoke(serviceUnderTest, 1L)
//        );
//    }
//
//    @Test
//    @DisplayName("Test validateSortProperties allows valid properties")
//    public void givenValidSortProperty_whenValidateSortProperties_thenNoException() throws Exception {
//        // given
//        Sort sort = Sort.by("model.brand.name"); // допустимое поле
//        var method = CarServiceImpl.class.getDeclaredMethod("validateSortProperties", Sort.class);
//        method.setAccessible(true);
//
//        // when + then (не бросает исключений)
//        method.invoke(serviceUnderTest, sort);
//    }
//
//
//
//
//    @Test
//    @DisplayName("Test getAllCars calls repository with proper filters")
//    public void givenValidFilters_whenGetAllCars_thenRepositoryIsCalled() {
//        // given
//        Pageable pageable = mock(Pageable.class);
//        Sort sort = Sort.by("model.brand.name");
//        given(pageable.getSort()).willReturn(sort);
//
//        CarFilterRequest filter = mock(CarFilterRequest.class);
//        given(filter.brands()).willReturn(List.of("BMW"));
//        given(filter.models()).willReturn(List.of("X5"));
//        given(filter.carClasses()).willReturn(List.of("SUV"));
//        given(filter.carState()).willReturn(List.of("AVAILABLE"));
//        given(filter.minYear()).willReturn(2015);
//        given(filter.maxYear()).willReturn(2023);
//        given(filter.bodyType()).willReturn("SUV");
//
//        Car carEntity = new Car();
//
//
//
//
//        CarListItemResponse responseDto = new CarListItemResponse(1L,"BMW", "X5", "SUV", 2000, 2009.3, "AVAILABLE", false);
//
//
//
//        Page<Car> pageResult = new PageImpl<>(List.of(carEntity));
//
//        given(carRepository.findByFilter(
//                anyList(),
//                anyList(),
//                anyInt(),
//                anyInt(),
//                anyString(),
//                anyList(),
//                anyList(),
//                any(),
//                any(),
//                any(),
//                any(),
//                any(Pageable.class)
//        )).willReturn(pageResult);
//
//        given(carMapper.toListItemDto(any(Car.class), false)).willReturn(responseDto);
//
//        // when
//        Page<CarListItemResponse> result = serviceUnderTest.getAllCars(pageable, filter);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).brand()).isEqualTo("BMW");
//
//        verify(carRepository).findByFilter(
//                eq(List.of("BMW")),
//                eq(List.of("X5")),
//                eq(2015),
//                eq(2023),
//                eq("SUV"),
//                eq(List.of("SUV")),
//                eq(List.of("AVAILABLE")),
//                any(),
//                any(),
//                any(),
//                any(),
//                eq(pageable)
//        );
//
//        verify(carMapper).toListItemDto(carEntity, false);
//    }
//
//    @Test
//    @DisplayName("Test getAllCars with empty filters passes nulls to repository")
//    public void givenEmptyFilters_whenGetAllCars_thenRepositoryReceivesNulls() {
//        // given
//        Pageable pageable = mock(Pageable.class);
//        given(pageable.getSort()).willReturn(Sort.by("model.brand.name"));
//
//        CarFilterRequest filter = mock(CarFilterRequest.class);
//        given(filter.brands()).willReturn(List.of());
//        given(filter.models()).willReturn(null);
//        given(filter.carClasses()).willReturn(List.of());
//        given(filter.carState()).willReturn(List.of());
//        given(filter.minYear()).willReturn(null);
//        given(filter.maxYear()).willReturn(null);
//        given(filter.bodyType()).willReturn(null);
//        given(filter.dateStart()).willReturn(null);
//        given(filter.dateEnd()).willReturn(null);
//        given(filter.minCell()).willReturn(null);
//        given(filter.maxCell()).willReturn(null);
//
//        Page<Car> pageResult = new PageImpl<>(List.of());
//        given(carRepository.findByFilter(
//                isNull(), isNull(), any(), any(), any(), isNull(), isNull(), any(), any(), any(), any(), any(Pageable.class)
//        )).willReturn(pageResult);
//
//        // when
//        Page<CarListItemResponse> result = serviceUnderTest.getAllCars(pageable, filter);
//
//        // then
//        assertThat(result).isNotNull();
//        verify(carRepository).findByFilter(
//                isNull(), isNull(), any(), any(), any(), isNull(), isNull(), any(), any(), any(), any(), eq(pageable)
//        );
//    }
//
//
//    @Test
//    @DisplayName("Test getAllCars with invalid sort property throws exception")
//    public void givenInvalidSortProperty_whenGetAllCars_thenThrowInvalidQueryParameterException() {
//        // given
//        Pageable pageable = mock(Pageable.class);
//        given(pageable.getSort()).willReturn(Sort.by("invalidField"));
//
//        CarFilterRequest filter = mock(CarFilterRequest.class);
//
//        // when + then
//        assertThrows(
//                InvalidQueryParameterException.class,
//                () -> serviceUnderTest.getAllCars(pageable, filter)
//        );
//
//        verify(carRepository, never()).findByFilter(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
//    }
//}
