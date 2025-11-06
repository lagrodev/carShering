package org.example.carshering.it.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarStateRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.entity.*;
import org.example.carshering.it.BaseWebIntegrateTest;
import org.example.carshering.repository.*;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminCarControllerIntegrationTests extends BaseWebIntegrateTest {


    private final DataUtils dataUtils = new DataUtils();

    private final String apiUrl = "/api/admin/cars";


    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarStateRepository carStateRepository;


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelNameRepository modelNameRepository;

    @Autowired
    private CarClassRepository carClassRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetSequences() {
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_model_id_model_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_state_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.brands_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.models_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_classes_id_seq RESTART WITH 1");
    }

    @BeforeEach
    @Transactional
    public void setup() {
        carRepository.deleteAll();
        carStateRepository.deleteAll();
        carModelRepository.deleteAll();
        carClassRepository.deleteAll();
        modelNameRepository.deleteAll();
        brandRepository.deleteAll();
    }



    private List<?> getCarStateAndCarModelAndSaveAllDependencies() {
        Brand brand = brandRepository
                .findByNameIgnoreCase(dataUtils.getBrandTransient().getName())
                .orElseGet(() -> brandRepository.save(dataUtils.getBrandTransient()));

        CarClass carClass = carClassRepository
                .findByNameIgnoreCase(dataUtils.getCarClassTransient().getName())
                .orElseGet(() -> carClassRepository.save(dataUtils.getCarClassTransient()));

        Model modelName = modelNameRepository
                .findByNameIgnoreCase(dataUtils.getModelNameTransient().getName())
                .orElseGet(() -> modelNameRepository.save(dataUtils.getModelNameTransient()));

        CarModel carModel = carModelRepository.save(
                dataUtils.getCarModelSEDAN(brand, modelName, carClass)
        );

        CarState carState = carStateRepository
                .findByStatusIgnoreCase(dataUtils.getCarStateTransient().getStatus())
                .orElseGet(() -> carStateRepository.save(dataUtils.getCarStateTransient()));

        return List.of(carState, carModel);
    }




    private List<?> getCarWithSpecificAttributes(
            String brandStr, String modelNameStr, String carClassStr, String carStateStr, String bodyType) {

        Brand brand = brandRepository
                .findByNameIgnoreCase(brandStr)
                .orElseGet(() -> brandRepository.save(dataUtils.getBrandTransient(brandStr)));

        CarClass carClass = carClassRepository
                .findByNameIgnoreCase(carClassStr)
                .orElseGet(() -> carClassRepository.save(dataUtils.getCarClassTransient(carClassStr)));

        Model modelName = modelNameRepository
                .findByNameIgnoreCase(modelNameStr)
                .orElseGet(() -> modelNameRepository.save(dataUtils.getModelNameTransient(modelNameStr)));

        CarModel carModel = carModelRepository.save(
                dataUtils.getCarModelBody(brand, modelName, carClass, bodyType)
        );

        CarState carState = carStateRepository
                .findByStatusIgnoreCase(carStateStr) // или findByNameIgnoreCase(carStateStr), если status хранится в name
                .orElseGet(() -> carStateRepository.save(dataUtils.getCarStateTransient(carStateStr)));

        return List.of(carState, carModel);
    }


    @Test
    @DisplayName("Test create car functionality")
    public void givenCarDto_whenCreateCar_thenSuccessResponse() throws Exception {

        dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));

        // given
        CreateCarRequest createCarRequest = dataUtils.createCarRequestTransient();


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
                .andExpect(jsonPath("$.status").value("Available"))
                .andExpect(jsonPath("$.rent").value(createCarRequest.rent()));
    }

    @Test
    @DisplayName("Test create car with invalid data functionality")
    public void givenInvalidCarDto_whenCreateCar_thenValidationErrorResponse() throws Exception {

        dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));


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
        String duplicateVin = createCarRequest.vin();

        Car car = dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));

        car.setVin(duplicateVin);
        carRepository.save(car);


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


        Car car = dataUtils.getMikeSmithTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));
        carRepository.save(car);

        UpdateCarRequest createCarRequest = dataUtils.updateCarRequestForVin();
        CarDetailResponse carDetailResponse = DataUtils.carDetailResponsePersisted();

        String updateVin = createCarRequest.vin();


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/" + car.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createCarRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.gosNumber").value(car.getGosNumber()))
                .andExpect(jsonPath("$.vin").value(updateVin))
                .andExpect(jsonPath("$.status").value(car.getState().getStatus()))
                .andExpect(jsonPath("$.rent").value(createCarRequest.rent()));
    }

    @Test
    @DisplayName("Test update car with incorrect functionality")
    public void givenCarDtoWithIncorrect_whenUpdateCar_thenErrorResponse() throws Exception {

        Car car = dataUtils.getMikeSmithPersisted(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));

        // given
        UpdateCarRequest createCarRequest = dataUtils.updateCarRequest();


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/" + car.getId())
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
        Car car = dataUtils.getMikeSmithTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));
        carRepository.save(car);

        // given
        UpdateCarRequest invalidRequest = new UpdateCarRequest(
                null, // brandId
                202, // gosNumber
                "",  // vin — пустой
                "",  // model — пустой
                50.0 // rent
        );

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/" + car.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(invalidRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("InvalidDataException")))
                .andExpect(jsonPath("$.message", is("VIN and Gos number cannot be blank")));
    }

    @Test
    @DisplayName("Test update car with invalid data functionality")
    public void givenInvalidUpdateCarDtoWhereRentIsBad_whenUpdateCar_thenValidationErrorResponse() throws Exception {
        Car car = dataUtils.getMikeSmithTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));
        carRepository.save(car);

        // given
        UpdateCarRequest invalidRequest = new UpdateCarRequest(
                null, // brandId
                202, // gosNumber
                "222",  // vin — пустой
                "111",  // model — пустой
                -50.0 // rent
        );

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/" + car.getId())
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
        Car car = dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));
        carRepository.save(car);
        // given
        CarDetailResponse carDetailResponse = DataUtils.carDetailResponsePersisted();


        // when

        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/" + car.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //  then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.gosNumber").value(carDetailResponse.gosNumber()))
                .andExpect(jsonPath("$.vin").value(carDetailResponse.vin()))
                .andExpect(jsonPath("$.status").value(car.getState().getStatus()))
                .andExpect(jsonPath("$.rent").value(carDetailResponse.rent()));
    }

    @Test
    @DisplayName("Test get car by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenErrorResponse() throws Exception {

        // given

        Car car = dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));
        carRepository.save(car);
        // when

        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/2")
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
//
//
    @Test
    @DisplayName("Test get car with non-numeric carId functionality")
    public void givenNonNumericCarId_whenGetCar_thenBadRequestResponse() throws Exception {
        Car car = dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));
        carRepository.save(car);
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

        Car car = dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));
        carRepository.save(car);


        carStateRepository.save( CarState.builder()
                .status("UNAVAILABLE")
                .build());

        Long carId = car.getId();


        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{carId}", carId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        Car optionalCar = carRepository.findById(carId).orElse(null);
        assertThat(optionalCar).isNotNull();
        assertThat(optionalCar.getState().getStatus()).isEqualTo("UNAVAILABLE");
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


        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{carId}", carId)
                .contentType(MediaType.APPLICATION_JSON));

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
    @DisplayName("Test update car state functionality")
    public void givenCarStateDto_whenUpdateCarState_thenSuccessResponse() throws Exception {
        Car car = dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));
        carRepository.save(car);

        Long carId = car.getId();

        carStateRepository.save( CarState.builder()
                .status("HUYNA")
                .build());



        CarStateResponse carStateResponse = dataUtils.getCarStateResponse();
        UpdateCarStateRequest request = dataUtils.getCarStateRequest("HUYNA");


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl +"/" + carId + "/state")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.status").value(request.stateName()));


    }
//
//
    @Test
    @DisplayName("Test update car state with invalid state name functionality")
    public void givenInvalidCarStateRequest_whenUpdateCarState_thenValidationErrorResponse() throws Exception {
        Car car = dataUtils.getJohnDoeTransient(
                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1));
        carRepository.save(car);

        Long carId = car.getId();
        // given
        UpdateCarStateRequest invalidRequest = new UpdateCarStateRequest(""); // пустое имя состояния

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl +"/" + carId + "/state")
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
        carStateRepository.saveAll(List.of(CarState.builder().status("AVAILABLE").build(), CarState.builder().status("BOOKED").build()));

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

        List<?> carAttributes1 = getCarWithSpecificAttributes("Tesla", "Model S", "PREMIUM", "AVAILABLE", "SEDAN");
        List<?> carAttributes2 = getCarWithSpecificAttributes("BMW", "X5", "PREMIUM", "BOOKED", "SUV");

        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "112123",  "332131",
                    2022,  100.0
                    );
        carRepository.save(carEntity1);


        Car carEntity2 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes2.get(0),
                (CarModel) carAttributes2.get(1),
            "11223",  "33231",
            2021, 150.0
        );


        carRepository.save(carEntity2);

        Page<CarListItemResponse> page = new PageImpl<>(List.of(car1, car2));


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[1].id").value(1L))
                .andExpect(jsonPath("$.content[1].brand").value("Tesla"))
                .andExpect(jsonPath("$.content[1].model").value("Model S"))
                .andExpect(jsonPath("$.content[1].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.content[1].rent").value(100.0))
                .andExpect(jsonPath("$.content[0].brand").value("BMW"))
                .andExpect(jsonPath("$.content[0].status").value("BOOKED"));
    }


    @Test
    @DisplayName("Test get cars with invalid filter years functionality")
    public void givenInvalidYearRange_whenGetCars_thenReturnOkEmptyList() throws Exception {

        // given
        Page<CarListItemResponse> emptyPage = new PageImpl<>(List.of());

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
//
//
    @Test
    @DisplayName("Test get all cars when empty result functionality")
    public void whenNoCarsFound_thenReturnEmptyPage() throws Exception {
        // given

        List<?> carAttributes1 = getCarWithSpecificAttributes("Tesla", "Model S", "PREMIUM", "AVAILABLE", "SEDAN");
        List<?> carAttributes2 = getCarWithSpecificAttributes("BMW", "X5", "PREMIUM", "BOOKED", "SUV");

        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "112123",  "332131",
                2022,  100.0
        );
        carRepository.save(carEntity1);


        Car carEntity2 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes2.get(0),
                (CarModel) carAttributes2.get(1),
                "11223",  "33231",
                2021, 150.0
        );


        carRepository.save(carEntity2);

        Page<CarListItemResponse> emptyPage = new PageImpl<>(List.of());


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
//
//
    @Test
    @DisplayName("Test get all car states when no states exist functionality")
    public void whenNoCarStatesExist_thenReturnEmptyList() throws Exception {

        // given

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/state")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }



    //
//    @Test
//    @DisplayName("Test get all cars with filters functionality")
//    public void givenFilters_whenGetCars_thenReturnFilteredPagedCars() throws Exception {
//        // given
//        CarListItemResponse car = new CarListItemResponse(3L, "Toyota", "ECONOMY", "Camry", 2020, 50.0, "AVAILABLE");
//        Page<CarListItemResponse> page = new PageImpl<>(List.of(car));
//
//
//        // when
//        ResultActions resultActions = mockMvc.perform(get(apiUrl)
//                .param("brand", "Toyota")
//                .param("minYear", "2019")
//                .param("maxYear", "2021")
//                .param("body_type", "SEDAN")
//                .param("car_class", "ECONOMY")
//                .param("car_state", "AVAILABLE")
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content[0].id").value(3L))
//                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
//                .andExpect(jsonPath("$.content[0].model").value("Camry"))
//                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"))
//                .andExpect(jsonPath("$.content[0].carClass").value("ECONOMY"))
//                .andExpect(jsonPath("$.content[0].yearOfIssue").value(2020));
//    }
//
//
    @Test
    @DisplayName("Test get cars with negative year filter functionality")
    public void givenNegativeYearFilter_whenGetCars_thenReturnOkWithEmptyList() throws Exception {

        // given
        Page<CarListItemResponse> emptyPage = new PageImpl<>(List.of());

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


    @Test
    @DisplayName("Test get cars filtered by brand functionality")
    public void givenBrandFilter_whenGetCars_thenReturnOnlyBrandMatchedCars() throws Exception {
        // given
        List<?> bmwAttributes = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SUV");
        Car bmwCar1 = dataUtils.getCarWithSpecificAttributes("VIN_BMW_1", "GOS_BMW_1", 2020,
                (CarState) bmwAttributes.get(0),
                (CarModel) bmwAttributes.get(1));
        carRepository.save(bmwCar1);

        List<?> bmwAttributes2 = getCarWithSpecificAttributes("BMW", "X3", "LUXURY", "AVAILABLE", "SUV");
        Car bmwCar2 = dataUtils.getCarWithSpecificAttributes("VIN_BMW_2", "GOS_BMW_2", 2021,
                (CarState) bmwAttributes2.get(0),
                (CarModel) bmwAttributes2.get(1));
        carRepository.save(bmwCar2);

        List<?> audiAttributes = getCarWithSpecificAttributes("AUDI", "A4", "STANDARD", "AVAILABLE", "SEDAN");
        Car audiCar = dataUtils.getCarWithSpecificAttributes("VIN_AUDI_1", "GOS_AUDI_1", 2022,
                (CarState) audiAttributes.get(0),
                (CarModel) audiAttributes.get(1));
        carRepository.save(audiCar);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("brand", "BMW")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[*].brand", org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("BMW"))));
    }


    @Test
    @DisplayName("Test get cars filtered by model functionality")
    public void givenModelFilter_whenGetCars_thenReturnOnlyModelMatchedCars() throws Exception {
        // given
        List<?> camryAttributes = getCarWithSpecificAttributes("TOYOTA", "CAMRY", "STANDARD", "AVAILABLE", "SEDAN");
        Car camry1 = dataUtils.getCarWithSpecificAttributes("VIN_CAMRY_1", "GOS_CAMRY_1", 2020,
                (CarState) camryAttributes.get(0),
                (CarModel) camryAttributes.get(1));
        carRepository.save(camry1);

        List<?> corollaAttributes = getCarWithSpecificAttributes("TOYOTA", "COROLLA", "STANDARD", "AVAILABLE", "SEDAN");
        Car corolla = dataUtils.getCarWithSpecificAttributes("VIN_COROLLA_1", "GOS_COROLLA_1", 2021,
                (CarState) corollaAttributes.get(0),
                (CarModel) corollaAttributes.get(1));
        carRepository.save(corolla);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("model", "CAMRY")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].model").value("CAMRY"))
                .andExpect(jsonPath("$.content[0].brand").value("TOYOTA"));
    }


    @Test
    @DisplayName("Test get cars filtered by year range functionality")
    public void givenYearRangeFilter_whenGetCars_thenReturnOnlyCarsWithinRange() throws Exception {
        // given
        List<?> car2020Attributes = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SUV");
        Car car2020 = dataUtils.getCarWithSpecificAttributes("VIN2020", "GOS2020", 2020,
                (CarState) car2020Attributes.get(0),
                (CarModel) car2020Attributes.get(1));
        carRepository.save(car2020);

        List<?> car2022Attributes = getCarWithSpecificAttributes("AUDI", "A4", "STANDARD", "AVAILABLE", "SEDAN");
        Car car2022 = dataUtils.getCarWithSpecificAttributes("VIN2022", "GOS2022", 2022,
                (CarState) car2022Attributes.get(0),
                (CarModel) car2022Attributes.get(1));
        carRepository.save(car2022);

        List<?> car2025Attributes = getCarWithSpecificAttributes("MERCEDES", "E", "LUXURY", "AVAILABLE", "SEDAN");
        Car car2025 = dataUtils.getCarWithSpecificAttributes("VIN2025", "GOS2025", 2025,
                (CarState) car2025Attributes.get(0),
                (CarModel) car2025Attributes.get(1));
        carRepository.save(car2025);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("minYear", "2021")
                .param("maxYear", "2023")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].yearOfIssue").value(2022));
    }


    @Test
    @DisplayName("Test get cars filtered by body type functionality")
    public void givenBodyTypeFilter_whenGetCars_thenReturnOnlyMatchingBodyType() throws Exception {
        // given
        List<?> sedanAttributes = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SEDAN");
        Car sedan = dataUtils.getCarWithSpecificAttributes("VIN_SEDAN", "GOS_SEDAN", 2020,
                (CarState) sedanAttributes.get(0),
                (CarModel) sedanAttributes.get(1));
        carRepository.save(sedan);

        List<?> suvAttributes = getCarWithSpecificAttributes("AUDI", "Q7", "LUXURY", "AVAILABLE", "SUV");
        Car suv = dataUtils.getCarWithSpecificAttributes("VIN_SUV", "GOS_SUV", 2021,
                (CarState) suvAttributes.get(0),
                (CarModel) suvAttributes.get(1));
        carRepository.save(suv);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("body_type", "SEDAN")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(sedan.getId()));
    }


    @Test
    @DisplayName("Test get cars filtered by car class functionality")
    public void givenCarClassFilter_whenGetCars_thenReturnOnlyMatchingClass() throws Exception {
        // given
        List<?> luxuryAttributes1 = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SUV");
        Car luxury1 = dataUtils.getCarWithSpecificAttributes("VIN_LUX_1", "GOS_LUX_1", 2020,
                (CarState) luxuryAttributes1.get(0),
                (CarModel) luxuryAttributes1.get(1));
        carRepository.save(luxury1);

        List<?> luxuryAttributes2 = getCarWithSpecificAttributes("MERCEDES", "E", "LUXURY", "AVAILABLE", "SEDAN");
        Car luxury2 = dataUtils.getCarWithSpecificAttributes("VIN_LUX_2", "GOS_LUX_2", 2021,
                (CarState) luxuryAttributes2.get(0),
                (CarModel) luxuryAttributes2.get(1));
        carRepository.save(luxury2);

        List<?> standardAttributes = getCarWithSpecificAttributes("TOYOTA", "COROLLA", "STANDARD", "AVAILABLE", "SEDAN");
        Car standard = dataUtils.getCarWithSpecificAttributes("VIN_STD", "GOS_STD", 2022,
                (CarState) standardAttributes.get(0),
                (CarModel) standardAttributes.get(1));
        carRepository.save(standard);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("car_class", "LUXURY")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[*].carClass", org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("LUXURY"))));
    }


    @Test
    @DisplayName("Test get cars filtered by car state functionality")
    public void givenCarStateFilter_whenGetCars_thenReturnOnlyMatchingState() throws Exception {
        // given
        List<?> availableAttributes = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SUV");
        Car available = dataUtils.getCarWithSpecificAttributes("VIN_AVAIL", "GOS_AVAIL", 2020,
                (CarState) availableAttributes.get(0),
                (CarModel) availableAttributes.get(1));
        carRepository.save(available);

        List<?> bookedAttributes = getCarWithSpecificAttributes("AUDI", "A4", "STANDARD", "BOOKED", "SEDAN");
        Car booked = dataUtils.getCarWithSpecificAttributes("VIN_BOOKED", "GOS_BOOKED", 2021,
                (CarState) bookedAttributes.get(0),
                (CarModel) bookedAttributes.get(1));
        carRepository.save(booked);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("car_state", "AVAILABLE")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"));
    }


    @Test
    @DisplayName("Test get cars with multiple filters functionality")
    public void givenMultipleFilters_whenGetCars_thenReturnMatchingCars() throws Exception {
        // given
        List<?> matchingAttributes = getCarWithSpecificAttributes("TOYOTA", "CAMRY", "STANDARD", "AVAILABLE", "SEDAN");
        Car matching = dataUtils.getCarWithSpecificAttributes("VIN_MATCH", "GOS_MATCH", 2020,
                (CarState) matchingAttributes.get(0),
                (CarModel) matchingAttributes.get(1));
        carRepository.save(matching);

        List<?> nonMatchingAttributes1 = getCarWithSpecificAttributes("TOYOTA", "COROLLA", "STANDARD", "AVAILABLE", "SEDAN");
        Car nonMatching1 = dataUtils.getCarWithSpecificAttributes("VIN_NO1", "GOS_NO1", 2020,
                (CarState) nonMatchingAttributes1.get(0),
                (CarModel) nonMatchingAttributes1.get(1));
        carRepository.save(nonMatching1);

        List<?> nonMatchingAttributes2 = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SUV");
        Car nonMatching2 = dataUtils.getCarWithSpecificAttributes("VIN_NO2", "GOS_NO2", 2021,
                (CarState) nonMatchingAttributes2.get(0),
                (CarModel) nonMatchingAttributes2.get(1));
        carRepository.save(nonMatching2);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("brand", "TOYOTA")
                .param("model", "CAMRY")
                .param("minYear", "2019")
                .param("maxYear", "2021")
                .param("body_type", "SEDAN")
                .param("car_class", "STANDARD")
                .param("car_state", "AVAILABLE")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(matching.getId()))
                .andExpect(jsonPath("$.content[0].brand").value("TOYOTA"))
                .andExpect(jsonPath("$.content[0].model").value("CAMRY"))
                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.content[0].carClass").value("STANDARD"))
                .andExpect(jsonPath("$.content[0].yearOfIssue").value(2020));
    }


    @Test
    @DisplayName("Test get cars with minYear only functionality")
    public void givenMinYearFilter_whenGetCars_thenReturnCarsFromMinYear() throws Exception {
        // given
        List<?> car2019Attributes = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SUV");
        Car car2019 = dataUtils.getCarWithSpecificAttributes("VIN2019", "GOS2019", 2019,
                (CarState) car2019Attributes.get(0),
                (CarModel) car2019Attributes.get(1));
        carRepository.save(car2019);

        List<?> car2022Attributes = getCarWithSpecificAttributes("AUDI", "A4", "STANDARD", "AVAILABLE", "SEDAN");
        Car car2022 = dataUtils.getCarWithSpecificAttributes("VIN2022", "GOS2022", 2022,
                (CarState) car2022Attributes.get(0),
                (CarModel) car2022Attributes.get(1));
        carRepository.save(car2022);

        List<?> car2024Attributes = getCarWithSpecificAttributes("MERCEDES", "E", "LUXURY", "AVAILABLE", "SEDAN");
        Car car2024 = dataUtils.getCarWithSpecificAttributes("VIN2024", "GOS2024", 2024,
                (CarState) car2024Attributes.get(0),
                (CarModel) car2024Attributes.get(1));
        carRepository.save(car2024);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("minYear", "2022")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[*].id", org.hamcrest.Matchers.containsInAnyOrder(
                        car2022.getId().intValue(), car2024.getId().intValue())));
    }


    @Test
    @DisplayName("Test get cars with maxYear only functionality")
    public void givenMaxYearFilter_whenGetCars_thenReturnCarsUpToMaxYear() throws Exception {
        // given
        List<?> car2019Attributes = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SUV");
        Car car2019 = dataUtils.getCarWithSpecificAttributes("VIN2019", "GOS2019", 2019,
                (CarState) car2019Attributes.get(0),
                (CarModel) car2019Attributes.get(1));
        carRepository.save(car2019);

        List<?> car2021Attributes = getCarWithSpecificAttributes("AUDI", "A4", "STANDARD", "AVAILABLE", "SEDAN");
        Car car2021 = dataUtils.getCarWithSpecificAttributes("VIN2021", "GOS2021", 2021,
                (CarState) car2021Attributes.get(0),
                (CarModel) car2021Attributes.get(1));
        carRepository.save(car2021);

        List<?> car2024Attributes = getCarWithSpecificAttributes("MERCEDES", "E", "LUXURY", "AVAILABLE", "SEDAN");
        Car car2024 = dataUtils.getCarWithSpecificAttributes("VIN2024", "GOS2024", 2024,
                (CarState) car2024Attributes.get(0),
                (CarModel) car2024Attributes.get(1));
        carRepository.save(car2024);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("maxYear", "2021")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[*].id", org.hamcrest.Matchers.containsInAnyOrder(
                        car2019.getId().intValue(), car2021.getId().intValue())));
    }


    @Test
    @DisplayName("Test get cars with exact year match functionality")
    public void givenMinYearEqualsMaxYear_whenGetCars_thenReturnOnlyExactYearCars() throws Exception {
        // given
        List<?> car2020Attributes = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SUV");
        Car car2020 = dataUtils.getCarWithSpecificAttributes("VIN2020", "GOS2020", 2020,
                (CarState) car2020Attributes.get(0),
                (CarModel) car2020Attributes.get(1));
        carRepository.save(car2020);

        List<?> car2021Attributes = getCarWithSpecificAttributes("AUDI", "A4", "STANDARD", "AVAILABLE", "SEDAN");
        Car car2021 = dataUtils.getCarWithSpecificAttributes("VIN2021", "GOS2021", 2021,
                (CarState) car2021Attributes.get(0),
                (CarModel) car2021Attributes.get(1));
        carRepository.save(car2021);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("minYear", "2021")
                .param("maxYear", "2021")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].yearOfIssue").value(2021));
    }


    @Test
    @DisplayName("Test get cars with no matching filters functionality")
    public void givenNoMatchingFilters_whenGetCars_thenReturnEmptyResult() throws Exception {
        // given
        List<?> carAttributes = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SUV");
        Car car = dataUtils.getCarWithSpecificAttributes("VIN_TEST", "GOS_TEST", 2020,
                (CarState) carAttributes.get(0),
                (CarModel) carAttributes.get(1));
        carRepository.save(car);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("brand", "NONEXISTENT_BRAND")
                .param("model", "NONEXISTENT_MODEL")
                .param("minYear", "2030")
                .param("maxYear", "2035")
                .param("body_type", "HATCHBACK")
                .param("car_class", "NONEXISTENT_CLASS")
                .param("car_state", "OLDSTATE")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(0)));
    }


    @Test
    @DisplayName("Test get cars pagination functionality")
    public void givenMultipleCars_whenGetCarsWithPagination_thenReturnCorrectPage() throws Exception {
        // given
        List<?> car1Attributes = getCarWithSpecificAttributes("BMW", "X5", "LUXURY", "AVAILABLE", "SUV");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) car1Attributes.get(0),
                (CarModel) car1Attributes.get(1));
        carRepository.save(car1);

        List<?> car2Attributes = getCarWithSpecificAttributes("AUDI", "A4", "STANDARD", "AVAILABLE", "SEDAN");
        Car car2 = dataUtils.getFrankJonesTransient(
                (CarState) car2Attributes.get(0),
                (CarModel) car2Attributes.get(1));
        carRepository.save(car2);

        List<?> car3Attributes = getCarWithSpecificAttributes("MERCEDES", "E", "LUXURY", "AVAILABLE", "SEDAN");
        Car car3 = dataUtils.getMikeSmithTransient(
                (CarState) car3Attributes.get(0),
                (CarModel) car3Attributes.get(1));
        carRepository.save(car3);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("size", "2")
                .param("page", "0")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }


    @Test
    @DisplayName("Big integration test: create 10 cars and filter returning 2 results")
    public void givenTenCars_whenFilter_thenReturnTwoMatchingCars() throws Exception {
        // given
        // Создаем 2 машины, которые должны пройти фильтр
        List<?> matchAttr1 = getCarWithSpecificAttributes("BRAND_MATCH", "MODEL_MATCH_A", "CLASS_MATCH", "AVAILABLE", "SEDAN");
        Car match1 = dataUtils.getCarWithSpecificAttributes("VIN_MATCH_1", "GOS_MATCH_1", 2021,
                (CarState) matchAttr1.get(0), (CarModel) matchAttr1.get(1));
        carRepository.save(match1);

        List<?> matchAttr2 = getCarWithSpecificAttributes("BRAND_MATCH", "MODEL_MATCH_B", "CLASS_MATCH", "AVAILABLE", "SEDAN");
        Car match2 = dataUtils.getCarWithSpecificAttributes("VIN_MATCH_2", "GOS_MATCH_2", 2022,
                (CarState) matchAttr2.get(0), (CarModel) matchAttr2.get(1));
        carRepository.save(match2);

        // Создаем 8 дополнительных машин, которые НЕ должны проходить фильтр
        for (int i = 0; i < 8; i++) {
            String vin = "VIN_OTHER_" + i;
            String gos = "GOS_OTHER_" + i;
            String brand = switch (i % 4) {
                case 0 -> "OTHER_BRAND_A";
                case 1 -> "OTHER_BRAND_B";
                case 2 -> "BRAND_MATCH"; // same brand but different class
                default -> "OTHER_BRAND_C";
            };
            String carClass = (i % 3 == 0) ? "OTHER_CLASS" : "ANOTHER_CLASS"; // ensure not CLASS_MATCH most of the time
            String model = "MODEL_OTHER_" + i;
            String body = (i % 2 == 0) ? "SUV" : "HATCHBACK";

            List<?> attrs = getCarWithSpecificAttributes(brand, model, carClass, "AVAILABLE", body);
            Car other = dataUtils.getCarWithSpecificAttributes(vin, gos, 2018 + (i % 6),
                    (CarState) attrs.get(0), (CarModel) attrs.get(1));
            carRepository.save(other);
        }

        // when
        // Фильтруем по бренду и классу — ожидаем ровно два совпадения
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("brand", "BRAND_MATCH")
                .param("car_class", "CLASS_MATCH")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[*].id", org.hamcrest.Matchers.containsInAnyOrder(
                        match1.getId().intValue(), match2.getId().intValue())));
    }


}
