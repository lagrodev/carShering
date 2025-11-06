package org.example.carshering.rest.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarStateRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.exceptions.custom.AlreadyExistsException;
import org.example.carshering.exceptions.custom.CarNotFoundException;
import org.example.carshering.rest.BaseWebMvcTest;
import org.example.carshering.service.CarService;
import org.example.carshering.service.CarStateService;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminCarController.class
)
@Import(LocalValidatorFactoryBean.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminCarControllerTests extends BaseWebMvcTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/admin/cars";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CarService carService;
    @MockitoBean
    private CarStateService carStateService;

    @Test
    @DisplayName("Test create car functionality")
    public void givenCarDto_whenCreateCar_thenSuccessResponse() throws Exception {

        // given
        CreateCarRequest createCarRequest = dataUtils.createCarRequestTransient();
        CarDetailResponse carDetailResponse = DataUtils.carDetailResponsePersisted();

        given(carService.createCar(any(CreateCarRequest.class))).willReturn(carDetailResponse);

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createCarRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.gosNumber").value(createCarRequest.gosNumber()))
                .andExpect(jsonPath("$.vin").value(createCarRequest.vin()))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.rent").value(createCarRequest.rent()));
    }

    @Test
    @DisplayName("Test create car with invalid data functionality")
    public void givenInvalidCarDto_whenCreateCar_thenValidationErrorResponse() throws Exception {

        // given
        CreateCarRequest invalidRequest = new CreateCarRequest(
                1L,
                202,// пустой госномер
                "",
                "",
                -100.0 // отрицательная аренда
        );

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(invalidRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("Test create car with duplicate vin functionality")
    public void givenCarDtoWithDuplicateGosNumber_whenCreateCar_thenErrorResponse() throws Exception {

        // given
        CreateCarRequest createCarRequest = dataUtils.createCarRequestTransient();

        given(carService.createCar(any(CreateCarRequest.class)))
                .willThrow(new AlreadyExistsException("VIN already exists"));

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createCarRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("AlreadyExistsException")))
                .andExpect(jsonPath("$.message").value("VIN already exists"))
        ;
    }

    @Test
    @DisplayName("Test update car functionality")
    public void givenCarDto_whenUpdateCar_thenSuccessResponse() throws Exception {

        // given
        UpdateCarRequest createCarRequest = dataUtils.updateCarRequest();
        CarDetailResponse carDetailResponse = DataUtils.carDetailResponsePersisted();

        given(carService.updateCar(eq(1L), any(UpdateCarRequest.class)))
                .willReturn(carDetailResponse);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createCarRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.gosNumber").value(createCarRequest.gosNumber()))
                .andExpect(jsonPath("$.vin").value(createCarRequest.vin()))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.rent").value(createCarRequest.rent()));
    }

    @Test
    @DisplayName("Test update car with incorrect functionality")
    public void givenCarDtoWithIncorrect_whenUpdateCar_thenErrorResponse() throws Exception {

        // given
        UpdateCarRequest createCarRequest = dataUtils.updateCarRequest();

        given(carService.updateCar(eq(1L), any(UpdateCarRequest.class)))
                .willThrow(new CarNotFoundException("Car not found"));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createCarRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("CarNotFoundException")))
                .andExpect(jsonPath("$.message").value("Car not found"))
        ;
    }



    @Test
    @DisplayName("Test update car with invalid data functionality")
    public void givenInvalidUpdateCarDto_whenUpdateCar_thenValidationErrorResponse() throws Exception {

        // given
        UpdateCarRequest invalidRequest = new UpdateCarRequest(
                null, // brandId
                202, // gosNumber
                "",  // vin — пустой
                "",  // model — пустой
                -50.0 // отрицательная аренда
        );

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(invalidRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }



    @Test
    @DisplayName("Test get car by id functionality")
    public void givenId_whenGetById_thenSuccessResponse() throws Exception {

        // given
        CarDetailResponse carDetailResponse = DataUtils.carDetailResponsePersisted();

        given(carService.getCarById(1L)).willReturn((carDetailResponse));

        // when

        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON));

        //  then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.gosNumber").value(carDetailResponse.gosNumber()))
                .andExpect(jsonPath("$.vin").value(carDetailResponse.vin()))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.rent").value(carDetailResponse.rent()));
    }

    @Test
    @DisplayName("Test get car by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenErrorResponse() throws Exception {

        // given

        given(carService.getCarById(1L)).willThrow((
                new CarNotFoundException("Car not found")
        ));

        // when

        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON));

        //  then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("CarNotFoundException")))
                .andExpect(jsonPath("$.message").value("Car not found"))
        ;
    }


    @Test
    @DisplayName("Test get car with non-numeric carId functionality")
    public void givenNonNumericCarId_whenGetCar_thenBadRequestResponse() throws Exception {

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/invalid-id")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'carId': 'invalid-id'")));
    }

    @Test
    @DisplayName("Test delete car by id functionality")
    public void givenCarId_whenDeleteCar_thenNoContentResponse() throws Exception {

        // given
        Long carId = 1L;

        doNothing().when(carService).deleteCar(eq(carId));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{carId}", carId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(carService, times(1)).deleteCar(carId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent())
        ;
    }

    @Test
    @DisplayName("Test delete car by incorrect id functionality")
    public void givenIncorrectCarId_whenDeleteCar_thenErrorResponse() throws Exception {

        // given
        Long carId = 1L;

        doThrow(new CarNotFoundException("Car not found"))
                .when(carService).deleteCar(eq(carId));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{carId}", carId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(carService, times(1)).deleteCar(carId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("CarNotFoundException")))
                .andExpect(jsonPath("$.message").value("Car not found"))
        ;
    }

    @Test
    @DisplayName("Test update car state functionality")
    public void givenCarStateDto_whenUpdateCarState_thenSuccessResponse() throws Exception {

        CarStateResponse carStateResponse = dataUtils.getCarStateResponse();
        UpdateCarStateRequest request = dataUtils.getCarStateRequest();

        given(carService.updateCarState(eq(1L), anyString()))
                .willReturn(carStateResponse);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/state")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value(request.stateName()));


    }


    @Test
    @DisplayName("Test update car state with invalid state name functionality")
    public void givenInvalidCarStateRequest_whenUpdateCarState_thenValidationErrorResponse() throws Exception {

        // given
        UpdateCarStateRequest invalidRequest = new UpdateCarStateRequest(""); // пустое имя состояния

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/state")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(invalidRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }


    @Test
    @DisplayName("Test update car state with incorrect functionality")
    public void givenCarStateIncorrectDto_whenUpdateCarState_thenErrorResponse() throws Exception {

        CarStateResponse carStateResponse = dataUtils.getCarStateResponse();
        UpdateCarStateRequest request = dataUtils.getCarStateRequest();

        given(carService.updateCarState(eq(1L), anyString()))
                .willThrow(new CarNotFoundException("Car not found"));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/state")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("CarNotFoundException")))
                .andExpect(jsonPath("$.message").value("Car not found"));

    }

    @Test
    @DisplayName("Test get all car states")
    public void whenGetAllCarStates_thenReturnListOfStates() throws Exception {
        // given
        CarStateResponse state1 = new CarStateResponse(1L, "AVAILABLE");
        CarStateResponse state2 = new CarStateResponse(2L, "BOOKED");

        given(carStateService.getAllStates()).willReturn(List.of(state1, state2));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/state")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[1].status").value("BOOKED"));
    }

    @Test
    @DisplayName("Test get all cars without filters functionality")
    public void whenGetAllCarsWithoutFilters_thenReturnPagedCars() throws Exception {
        // given
        CarListItemResponse car1 = new CarListItemResponse(1L, "Tesla", "BUSINESS", "Model S", 2022, 100.0, "AVAILABLE");
        CarListItemResponse car2 = new CarListItemResponse(2L, "BMW", "PREMIUM", "X5", 2021, 150.0, "BOOKED");

        Page<CarListItemResponse> page = new org.springframework.data.domain.PageImpl<>(List.of(car1, car2));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].brand").value("Tesla"))
                .andExpect(jsonPath("$.content[0].model").value("Model S"))
                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.content[0].rent").value(100.0))
                .andExpect(jsonPath("$.content[1].brand").value("BMW"))
                .andExpect(jsonPath("$.content[1].status").value("BOOKED"));
    }


    @Test
    @DisplayName("Test get all cars with filters functionality")
    public void givenFilters_whenGetCars_thenReturnFilteredPagedCars() throws Exception {
        // given
        CarListItemResponse car = new CarListItemResponse(3L, "Toyota", "ECONOMY", "Camry", 2020, 50.0, "AVAILABLE");
        Page<CarListItemResponse> page = new org.springframework.data.domain.PageImpl<>(List.of(car));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("brand", "Toyota")
                .param("minYear", "2019")
                .param("maxYear", "2021")
                .param("body_type", "SEDAN")
                .param("car_class", "ECONOMY")
                .param("car_state", "AVAILABLE")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3L))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].model").value("Camry"))
                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.content[0].carClass").value("ECONOMY"))
                .andExpect(jsonPath("$.content[0].yearOfIssue").value(2020));
    }


    @Test
    @DisplayName("Test get cars with invalid filter years functionality")
    public void givenInvalidYearRange_whenGetCars_thenReturnOkEmptyList() throws Exception {

        // given
        Page<CarListItemResponse> emptyPage = new PageImpl<>(List.of());
        given(carService.getAllCars(any(Pageable.class), any())).willReturn(emptyPage);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("minYear", "2025")
                .param("maxYear", "2015")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(0)));
    }


    @Test
    @DisplayName("Test get all cars when empty result functionality")
    public void whenNoCarsFound_thenReturnEmptyPage() throws Exception {
        // given
        Page<CarListItemResponse> emptyPage = new org.springframework.data.domain.PageImpl<>(List.of());

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(emptyPage);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("brand", "UnknownBrand")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(0)));
    }


    @Test
    @DisplayName("Test get all car states when no states exist functionality")
    public void whenNoCarStatesExist_thenReturnEmptyList() throws Exception {

        // given
        given(carStateService.getAllStates()).willReturn(List.of());

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/state")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @DisplayName("Test get cars with negative year filter functionality")
    public void givenNegativeYearFilter_whenGetCars_thenReturnOkWithEmptyList() throws Exception {

        // given
        Page<CarListItemResponse> emptyPage = new PageImpl<>(List.of());
        given(carService.getAllCars(any(Pageable.class), any())).willReturn(emptyPage);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("minYear", "-5")
                .param("maxYear", "2025")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(0)));
    }


}
