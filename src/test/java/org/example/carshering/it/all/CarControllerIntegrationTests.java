package org.example.carshering.it.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.domain.entity.*;
import org.example.carshering.it.BaseWebIntegrateTest;
import org.example.carshering.repository.*;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerIntegrationTests extends BaseWebIntegrateTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/car";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelNameRepository modelNameRepository;

    @Autowired
    private CarClassRepository carClassRepository;

    @Autowired
    private CarStateRepository carStateRepository;

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
                .findByStatusIgnoreCase(carStateStr)
                .orElseGet(() -> carStateRepository.save(dataUtils.getCarStateTransient(carStateStr)));

        return List.of(carState, carModel);
    }

    @Test
    @DisplayName("Test get catalogue functionality")
    public void givenNoFilters_whenGetCatalogue_thenSuccessResponse() throws Exception {

        // given
        List<?> dependencies1 = getCarWithSpecificAttributes("Toyota", "Camry", "Comfort", "AVAILABLE", "SEDAN");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) dependencies1.get(0),
                (CarModel) dependencies1.get(1)
        );
        car1.setYearOfIssue(2020);
        car1.setRent(50.0);
        carRepository.save(car1);

        List<?> dependencies2 = getCarWithSpecificAttributes("Honda", "Civic", "Economy", "AVAILABLE", "SEDAN");
        Car car2 = dataUtils.getMikeSmithTransient(
                (CarState) dependencies2.get(0),
                (CarModel) dependencies2.get(1)
        );
        car2.setYearOfIssue(2021);
        car2.setRent(45.0);
        carRepository.save(car2);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[1].id").value(1L))
                .andExpect(jsonPath("$.content[1].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[1].model").value("Camry"))
                .andExpect(jsonPath("$.content[1].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.content[0].id").value(2L))
                .andExpect(jsonPath("$.content[0].brand").value("Honda"));
    }

    @Test
    @DisplayName("Test get catalogue with brand filter functionality")
    public void givenBrandFilter_whenGetCatalogue_thenFilteredResponse() throws Exception {

        // given
        List<?> dependencies = getCarWithSpecificAttributes("Toyota", "Camry", "Comfort", "AVAILABLE", "SEDAN");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) dependencies.get(0),
                (CarModel) dependencies.get(1)
        );
        car1.setYearOfIssue(2020);
        car1.setRent(50.0);
        carRepository.save(car1);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .param("brand", "Toyota")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"));
    }

    @Test
    @DisplayName("Test get catalogue with multiple filters functionality")
    public void givenMultipleFilters_whenGetCatalogue_thenFilteredResponse() throws Exception {

        // given
        List<?> dependencies = getCarWithSpecificAttributes("Toyota", "Camry", "Comfort", "AVAILABLE", "SEDAN");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) dependencies.get(0),
                (CarModel) dependencies.get(1)
        );
        car1.setYearOfIssue(2020);
        car1.setRent(50.0);
        carRepository.save(car1);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .param("brand", "Toyota")
                .param("minYear", "2019")
                .param("maxYear", "2021")
                .param("car_class", "Comfort")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].yearOfIssue").value(2020))
                .andExpect(jsonPath("$.content[0].carClass").value("Comfort"));
    }

    @Test
    @DisplayName("Test get catalogue with year range filter functionality")
    public void givenYearRangeFilter_whenGetCatalogue_thenFilteredResponse() throws Exception {

        // given
        List<?> dependencies = getCarWithSpecificAttributes("Toyota", "Camry", "Comfort", "AVAILABLE", "SEDAN");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) dependencies.get(0),
                (CarModel) dependencies.get(1)
        );
        car1.setYearOfIssue(2020);
        car1.setRent(50.0);
        carRepository.save(car1);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .param("minYear", "2018")
                .param("maxYear", "2022")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].yearOfIssue").value(2020));
    }

    @Test
    @DisplayName("Test get catalogue with body type filter functionality")
    public void givenBodyTypeFilter_whenGetCatalogue_thenFilteredResponse() throws Exception {

        // given
        List<?> dependencies = getCarWithSpecificAttributes("Toyota", "Camry", "Comfort", "AVAILABLE", "SEDAN");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) dependencies.get(0),
                (CarModel) dependencies.get(1)
        );
        car1.setYearOfIssue(2020);
        car1.setRent(50.0);
        carRepository.save(car1);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .param("body_type", "SEDAN")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    @DisplayName("Test get catalogue with multiple brands functionality")
    public void givenMultipleBrands_whenGetCatalogue_thenFilteredResponse() throws Exception {

        // given
        List<?> dependencies1 = getCarWithSpecificAttributes("Toyota", "Camry", "Comfort", "AVAILABLE", "SEDAN");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) dependencies1.get(0),
                (CarModel) dependencies1.get(1)
        );
        car1.setYearOfIssue(2020);
        car1.setRent(50.0);
        carRepository.save(car1);

        List<?> dependencies2 = getCarWithSpecificAttributes("Honda", "Civic", "Economy", "AVAILABLE", "SEDAN");
        Car car2 = dataUtils.getMikeSmithTransient(
                (CarState) dependencies2.get(0),
                (CarModel) dependencies2.get(1)
        );
        car2.setYearOfIssue(2021);
        car2.setRent(45.0);
        carRepository.save(car2);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .param("brand", "Toyota,Honda")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Test get catalogue with pagination functionality")
    public void givenPageable_whenGetCatalogue_thenPaginatedResponse() throws Exception {

        // given
        List<?> dependencies = getCarWithSpecificAttributes("Toyota", "Camry", "Comfort", "AVAILABLE", "SEDAN");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) dependencies.get(0),
                (CarModel) dependencies.get(1)
        );
        car1.setYearOfIssue(2020);
        car1.setRent(50.0);
        carRepository.save(car1);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.page", notNullValue()));
    }

    @Test
    @DisplayName("Test get car by id functionality")
    public void givenCarId_whenGetCar_thenSuccessResponse() throws Exception {

        // given
        List<?> dependencies = getCarStateAndCarModelAndSaveAllDependencies();
        Car car = dataUtils.getJohnDoeTransient(
                (CarState) dependencies.get(0),
                (CarModel) dependencies.get(1)
        );
        car.setYearOfIssue(2020);
        car.setRent(50.0);
        carRepository.save(car);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.gosNumber").value(car.getGosNumber()))
                .andExpect(jsonPath("$.vin").value(car.getVin()))
                .andExpect(jsonPath("$.status").value(car.getState().getStatus()))
                .andExpect(jsonPath("$.rent").value(50.0));
    }

    @Test
    @DisplayName("Test get car by incorrect id functionality")
    public void givenIncorrectCarId_whenGetCar_thenErrorResponse() throws Exception {

        // given

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/999")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("CarNotFoundException")))
                .andExpect(jsonPath("$.message").value("Car not found"));
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
    @DisplayName("Test get all brands functionality")
    public void givenRequest_whenGetBrands_thenSuccessResponse() throws Exception {

        // given
        brandRepository.save(dataUtils.getBrandTransient("Toyota"));
        brandRepository.save(dataUtils.getBrandTransient("Honda"));
        brandRepository.save(dataUtils.getBrandTransient("BMW"));
        brandRepository.save(dataUtils.getBrandTransient("Mercedes"));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/brands")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value("Toyota"))
                .andExpect(jsonPath("$[1]").value("Honda"))
                .andExpect(jsonPath("$[2]").value("BMW"))
                .andExpect(jsonPath("$[3]").value("Mercedes"));
    }

    @Test
    @DisplayName("Test get all brands when empty functionality")
    public void givenNoBrands_whenGetBrands_thenEmptyListResponse() throws Exception {

        // given

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/brands")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Test get all models functionality")
    public void givenRequest_whenGetModels_thenSuccessResponse() throws Exception {

        // given
        modelNameRepository.save(dataUtils.getModelNameTransient("Camry"));
        modelNameRepository.save(dataUtils.getModelNameTransient("Civic"));
        modelNameRepository.save(dataUtils.getModelNameTransient("X5"));
        modelNameRepository.save(dataUtils.getModelNameTransient("E-Class"));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/models")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value("Camry"))
                .andExpect(jsonPath("$[1]").value("Civic"));
    }

    @Test
    @DisplayName("Test get all classes functionality")
    public void givenRequest_whenGetClasses_thenSuccessResponse() throws Exception {

        // given
        carClassRepository.save(dataUtils.getCarClassTransient("Economy"));
        carClassRepository.save(dataUtils.getCarClassTransient("Comfort"));
        carClassRepository.save(dataUtils.getCarClassTransient("Business"));
        carClassRepository.save(dataUtils.getCarClassTransient("Premium"));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/classes")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value("Economy"))
                .andExpect(jsonPath("$[1]").value("Comfort"))
                .andExpect(jsonPath("$[2]").value("Business"))
                .andExpect(jsonPath("$[3]").value("Premium"));
    }

    @Test
    @DisplayName("Test get all body types functionality")
    public void givenRequest_whenGetBodyTypes_thenSuccessResponse() throws Exception {

        // given
        Brand brand = brandRepository.save(dataUtils.getBrandTransient());
        CarClass carClass = carClassRepository.save(dataUtils.getCarClassTransient());
        Model modelName = modelNameRepository.save(dataUtils.getModelNameTransient());

        carModelRepository.save(dataUtils.getCarModelBody(brand, modelName, carClass, "SEDAN"));
        carModelRepository.save(dataUtils.getCarModelBody(brand, modelName, carClass, "SUV"));
        carModelRepository.save(dataUtils.getCarModelBody(brand, modelName, carClass, "HATCHBACK"));
        carModelRepository.save(dataUtils.getCarModelBody(brand, modelName, carClass, "COUPE"));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/body-types")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$", containsInAnyOrder("SUV", "SEDAN", "HATCHBACK", "COUPE")));
        ;
    }

    @Test
    @DisplayName("Test get body types when empty functionality")
    public void givenNoBodyTypes_whenGetBodyTypes_thenEmptyListResponse() throws Exception {

        // given

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/body-types")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Test get catalogue with model filter functionality")
    public void givenModelFilter_whenGetCatalogue_thenFilteredResponse() throws Exception {

        // given
        List<?> dependencies = getCarWithSpecificAttributes("Toyota", "Camry", "Comfort", "AVAILABLE", "SEDAN");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) dependencies.get(0),
                (CarModel) dependencies.get(1)
        );
        car1.setYearOfIssue(2020);
        car1.setRent(50.0);
        carRepository.save(car1);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .param("model", "Camry")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].model").value("Camry"));
    }

    @Test
    @DisplayName("Test get catalogue with car class filter functionality")
    public void givenCarClassFilter_whenGetCatalogue_thenFilteredResponse() throws Exception {

        // given
        List<?> dependencies = getCarWithSpecificAttributes("Toyota", "Camry", "Comfort", "AVAILABLE", "SEDAN");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) dependencies.get(0),
                (CarModel) dependencies.get(1)
        );
        car1.setYearOfIssue(2020);
        car1.setRent(50.0);
        carRepository.save(car1);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .param("car_class", "Comfort")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].carClass").value("Comfort"));
    }

    @Test
    @DisplayName("Test get catalogue with all filters functionality")
    public void givenAllFilters_whenGetCatalogue_thenFilteredResponse() throws Exception {

        // given
        List<?> dependencies = getCarWithSpecificAttributes("Toyota", "Camry", "Comfort", "AVAILABLE", "SEDAN");
        Car car1 = dataUtils.getJohnDoeTransient(
                (CarState) dependencies.get(0),
                (CarModel) dependencies.get(1)
        );
        car1.setYearOfIssue(2020);
        car1.setRent(50.0);
        carRepository.save(car1);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .param("brand", "Toyota")
                .param("model", "Camry")
                .param("minYear", "2019")
                .param("maxYear", "2021")
                .param("body_type", "SEDAN")
                .param("car_class", "Comfort")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].model").value("Camry"))
                .andExpect(jsonPath("$.content[0].carClass").value("Comfort"))
                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"));
    }
}

