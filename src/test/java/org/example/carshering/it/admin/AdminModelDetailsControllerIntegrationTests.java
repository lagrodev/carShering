package org.example.carshering.it.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.fleet.api.dto.request.create.CreateCarModelName;
import org.example.carshering.fleet.api.dto.request.create.CreateCarModelRequest;
import org.example.carshering.fleet.api.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.fleet.api.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.fleet.api.dto.responce.BrandModelResponse;
import org.example.carshering.fleet.api.dto.responce.CarModelResponse;
import org.example.carshering.fleet.api.dto.responce.ModelNameResponse;
import org.example.carshering.fleet.infrastructure.persistence.entity.Brand;
import org.example.carshering.fleet.infrastructure.persistence.entity.CarClass;
import org.example.carshering.fleet.infrastructure.persistence.entity.CarModel;
import org.example.carshering.fleet.infrastructure.persistence.entity.Model;
import org.example.carshering.fleet.infrastructure.persistence.repository.*;
import org.example.carshering.it.BaseWebIntegrateTest;
import org.example.carshering.util.DataUtils;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminModelDetailsControllerIntegrationTests extends BaseWebIntegrateTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/admin";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelNameRepository modelNameRepository;

    @Autowired
    private CarClassRepository carClassRepository;

    @BeforeEach
    void resetSequences() {
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_model_id_model_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.brands_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.models_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_classes_id_seq RESTART WITH 1");
    }

    @Autowired
    private CarRepository carRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        carRepository.deleteAll();
        carModelRepository.deleteAll();
        carClassRepository.deleteAll();
        modelNameRepository.deleteAll();
        brandRepository.deleteAll();
    }

    private List<?> createCarModelDependencies(String brandStr, String modelNameStr, String carClassStr) {
        Brand brand = brandRepository
                .findByNameIgnoreCase(brandStr)
                .orElseGet(() -> brandRepository.save(dataUtils.getBrandTransient(brandStr)));

        CarClass carClass = carClassRepository
                .findByNameIgnoreCase(carClassStr)
                .orElseGet(() -> carClassRepository.save(dataUtils.getCarClassTransient(carClassStr)));

        Model modelName = modelNameRepository
                .findByNameIgnoreCase(modelNameStr)
                .orElseGet(() -> modelNameRepository.save(dataUtils.getModelNameTransient(modelNameStr)));

        return List.of(brand, modelName, carClass);
    }

    @Test
    @DisplayName("Test get all models without filters functionality")
    public void whenGetAllModelsWithoutFilters_thenReturnPagedModels() throws Exception {

        // given
        List<?> deps1 = createCarModelDependencies("Toyota", "Camry", "BUSINESS");
        CarModel carModel1 = carModelRepository.save(
                dataUtils.getCarModelBody((Brand) deps1.get(0), (Model) deps1.get(1),
                        (CarClass) deps1.get(2), "SEDAN")
        );

        List<?> deps2 = createCarModelDependencies("BMW", "X5", "PREMIUM");
        CarModel carModel2 = carModelRepository.save(
                dataUtils.getCarModelBody((Brand) deps2.get(0), (Model) deps2.get(1),
                        (CarClass) deps2.get(2), "SUV")
        );

        CarModelResponse model1 = new CarModelResponse(1L, "Toyota", "Camry", "SEDAN", "BUSINESS", false);
        CarModelResponse model2 = new CarModelResponse(2L, "BMW", "X5", "SUV", "PREMIUM", false);
        Page<CarModelResponse> page = new PageImpl<>(List.of(model1, model2));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/models")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[1].modelId").value(1L))
                .andExpect(jsonPath("$.content[1].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[1].model").value("Camry"))
                .andExpect(jsonPath("$.content[1].bodyType").value("SEDAN"))
                .andExpect(jsonPath("$.content[1].carClass").value("BUSINESS"))
                .andExpect(jsonPath("$.content[1].isDeleted").value(false))
                .andExpect(jsonPath("$.content[0].brand").value("BMW"));
    }

    @Test
    @DisplayName("Test get all models with filters functionality")
    public void givenFilters_whenGetModels_thenReturnFilteredPagedModels() throws Exception {

        // given
        List<?> deps1 = createCarModelDependencies("Mercedes", "E-Class", "PREMIUM");
        CarModel carModel1 = carModelRepository.save(
                dataUtils.getCarModelBody((Brand) deps1.get(0), (Model) deps1.get(1),
                        (CarClass) deps1.get(2), "SEDAN")
        );

        List<?> deps2 = createCarModelDependencies("BMW", "X5", "PREMIUM");
        CarModel carModel2 = carModelRepository.save(
                dataUtils.getCarModelBody((Brand) deps2.get(0), (Model) deps2.get(1),
                        (CarClass) deps2.get(2), "SUV")
        );

        CarModelResponse model = new CarModelResponse(1L, "Mercedes", "E-Class", "SEDAN", "PREMIUM", false);
        Page<CarModelResponse> page = new PageImpl<>(List.of(model));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/models")
                .param("brand", "Mercedes")
                .param("body_type", "SEDAN")
                .param("car_class", "PREMIUM")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].modelId").value(1L))
                .andExpect(jsonPath("$.content[0].brand").value("Mercedes"))
                .andExpect(jsonPath("$.content[0].model").value("E-Class"))
                .andExpect(jsonPath("$.content[0].bodyType").value("SEDAN"))
                .andExpect(jsonPath("$.content[0].carClass").value("PREMIUM"));
    }

    @Test
    @DisplayName("Test get all models when empty result functionality")
    public void whenNoModelsFound_thenReturnEmptyPage() throws Exception {

        // given
        Page<CarModelResponse> emptyPage = new PageImpl<>(List.of());

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/models")
                .param("brand", "UnknownBrand")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @DisplayName("Test get model by id functionality")
    public void givenId_whenGetModelById_thenSuccessResponse() throws Exception {

        // given
        List<?> deps = createCarModelDependencies("Toyota", "Camry", "BUSINESS");
        CarModel carModel = carModelRepository.save(
                dataUtils.getCarModelBody((Brand) deps.get(0), (Model) deps.get(1),
                        (CarClass) deps.get(2), "SEDAN")
        );

        CarModelResponse modelResponse = new CarModelResponse(1L, "Toyota", "Camry", "SEDAN", "BUSINESS", false);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/models/1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelId", notNullValue()))
                .andExpect(jsonPath("$.modelId").value(1L))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.bodyType").value("SEDAN"))
                .andExpect(jsonPath("$.carClass").value("BUSINESS"));
    }

    @Test
    @DisplayName("Test get model by incorrect id functionality")
    public void givenIncorrectId_whenGetModelById_thenErrorResponse() throws Exception {

        // given

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/models/999")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("EntityNotFoundException")))
                .andExpect(jsonPath("$.message").value("Model not found"));
    }

    @Test
    @DisplayName("Test get model with non-numeric modelId functionality")
    public void givenNonNumericModelId_whenGetModel_thenBadRequestResponse() throws Exception {

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/models/invalid-id")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'modelId': 'invalid-id'")));
    }

    @Test
    @DisplayName("Test create model functionality")
    public void givenCarModelDto_whenCreateModel_thenSuccessResponse() throws Exception {

        // given
        createCarModelDependencies("Toyota", "Camry", "BUSINESS");

        CreateCarModelRequest createRequest = new CreateCarModelRequest("Toyota", "Camry", "SEDAN", "BUSINESS");
        CarModelResponse modelResponse = new CarModelResponse(1L, "Toyota", "Camry", "SEDAN", "BUSINESS", false);

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/models")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.modelId", notNullValue()))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.bodyType").value("SEDAN"))
                .andExpect(jsonPath("$.carClass").value("BUSINESS"));
    }

    @Test
    @DisplayName("Test create model with invalid data functionality")
    public void givenInvalidCarModelDto_whenCreateModel_thenValidationErrorResponse() throws Exception {

        // given
        CreateCarModelRequest invalidRequest = new CreateCarModelRequest(
                "", // пустой бренд
                "", // пустая модель
                "", // пустой тип кузова
                ""  // пустой класс
        );

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/models")
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
    @DisplayName("Test create model with duplicate data functionality")
    public void givenCarModelDtoWithDuplicateData_whenCreateModel_thenErrorResponse() throws Exception {

        // given
        List<?> deps = createCarModelDependencies("Toyota", "Camry", "BUSINESS");
        CarModel existingModel = carModelRepository.save(
                dataUtils.getCarModelBody((Brand) deps.get(0), (Model) deps.get(1),
                        (CarClass) deps.get(2), "SEDAN")
        );

        CreateCarModelRequest createRequest = new CreateCarModelRequest("Toyota", "Camry", "SEDAN", "BUSINESS");

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/models")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("AlreadyExistsException")))
                .andExpect(jsonPath("$.message").value("Model already exists"));
    }

    @Test
    @DisplayName("Test update model functionality")
    public void givenCarModelDto_whenUpdateModel_thenSuccessResponse() throws Exception {

        // given
        List<?> deps = createCarModelDependencies("Toyota", "Camry", "BUSINESS");
        CarModel carModel = carModelRepository.save(
                dataUtils.getCarModelBody((Brand) deps.get(0), (Model) deps.get(1),
                        (CarClass) deps.get(2), "SEDAN")
        );

        createCarModelDependencies("Toyota", "Camry", "PREMIUM");

        UpdateCarModelRequest updateRequest = new UpdateCarModelRequest("Toyota", "Camry", "SEDAN", "PREMIUM");
        CarModelResponse modelResponse = new CarModelResponse(1L, "Toyota", "Camry", "SEDAN", "PREMIUM", false);

        // when
        ResultActions resultActions = mockMvc.perform(put(apiUrl + "/models/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelId", notNullValue()))
                .andExpect(jsonPath("$.modelId").value(1L))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.bodyType").value("SEDAN"))
                .andExpect(jsonPath("$.carClass").value("PREMIUM"));
    }

    @Test
    @DisplayName("Test update model with incorrect id functionality")
    public void givenCarModelDtoWithIncorrectId_whenUpdateModel_thenErrorResponse() throws Exception {

        // given
        UpdateCarModelRequest updateRequest = new UpdateCarModelRequest("Toyota", "Camry", "SEDAN", "BUSINESS");

        // when
        ResultActions resultActions = mockMvc.perform(put(apiUrl + "/models/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("EntityNotFoundException")))
                .andExpect(jsonPath("$.message").value("Model not found"));
    }

    @Test
    @DisplayName("Test update model with invalid data functionality")
    public void givenInvalidUpdateCarModelDto_whenUpdateModel_thenValidationErrorResponse() throws Exception {

        // given
        UpdateCarModelRequest invalidRequest = new UpdateCarModelRequest(
                "", // пустой бренд
                "", // пустая модель
                "", // пустой тип кузова
                ""  // пустой класс
        );

        // when
        ResultActions resultActions = mockMvc.perform(put(apiUrl + "/models/1")
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
    @DisplayName("Test delete model by id functionality")
    public void givenModelId_whenDeleteModel_thenNoContentResponse() throws Exception {

        // given
        List<?> deps = createCarModelDependencies("Toyota", "Camry", "BUSINESS");
        CarModel carModel = carModelRepository.save(
                dataUtils.getCarModelBody((Brand) deps.get(0), (Model) deps.get(1),
                        (CarClass) deps.get(2), "SEDAN")
        );

        Long modelId = carModel.getIdModel();

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/models/{modelId}", modelId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test delete model by incorrect id functionality")
    public void givenIncorrectModelId_whenDeleteModel_thenErrorResponse() throws Exception {

        // given
        Long modelId = 999L;

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/models/{modelId}", modelId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("EntityNotFoundException")))
                .andExpect(jsonPath("$.message").value("Model not found"));
    }

    @Test
    @DisplayName("Test create brand functionality")
    public void givenBrandDto_whenCreateBrand_thenSuccessResponse() throws Exception {

        // given
        CreateCarModelsBrand createBrandRequest = new CreateCarModelsBrand("Mercedes");
        BrandModelResponse brandResponse = new BrandModelResponse("Mercedes");

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/filters/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createBrandRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("Mercedes"));
    }

    @Test
    @DisplayName("Test create brand with invalid data functionality")
    public void givenInvalidBrandDto_whenCreateBrand_thenValidationErrorResponse() throws Exception {

        // given
        CreateCarModelsBrand invalidRequest = new CreateCarModelsBrand(""); // пустое имя бренда

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/filters/brands")
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
    @DisplayName("Test get all brands functionality")
    public void whenGetAllBrands_thenReturnListOfBrands() throws Exception {

        // given
        brandRepository.save(dataUtils.getBrandTransient("Toyota"));
        brandRepository.save(dataUtils.getBrandTransient("BMW"));
        brandRepository.save(dataUtils.getBrandTransient("Mercedes"));

        List<String> brands = List.of("Toyota", "BMW", "Mercedes");

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/brands")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value("BMW"))
                .andExpect(jsonPath("$[2]").value("Mercedes"))
                .andExpect(jsonPath("$[0]").value("Toyota"));
    }

    @Test
    @DisplayName("Test get all brands when no brands exist functionality")
    public void whenNoBrandsExist_thenReturnEmptyList() throws Exception {

        // given

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/brands")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @DisplayName("Test create model name functionality")
    public void givenModelNameDto_whenCreateModelName_thenSuccessResponse() throws Exception {

        // given
        CreateCarModelName createModelNameRequest = new CreateCarModelName("Camry");
        ModelNameResponse modelNameResponse = new ModelNameResponse("Camry");

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/filters/models")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createModelNameRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Camry"));
    }

    @Test
    @DisplayName("Test create model name with invalid data functionality")
    public void givenInvalidModelNameDto_whenCreateModelName_thenValidationErrorResponse() throws Exception {

        // given
        CreateCarModelName invalidRequest = new CreateCarModelName(""); // пустое имя модели

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/filters/models")
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
    @DisplayName("Test get all model names functionality")
    public void whenGetAllModelNames_thenReturnListOfModelNames() throws Exception {

        // given
        modelNameRepository.save(dataUtils.getModelNameTransient("Camry"));
        modelNameRepository.save(dataUtils.getModelNameTransient("Corolla"));
        modelNameRepository.save(dataUtils.getModelNameTransient("X5"));
        modelNameRepository.save(dataUtils.getModelNameTransient("3 Series"));

        List<String> modelNames = List.of("Camry", "Corolla", "X5", "3 Series");

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/models")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[3]").value("3 Series"))
                .andExpect(jsonPath("$[0]").value("Camry"))
                .andExpect(jsonPath("$[1]").value("Corolla"))
                .andExpect(jsonPath("$[2]").value("X5"));
    }

    @Test
    @DisplayName("Test get all model names when no models exist functionality")
    public void whenNoModelNamesExist_thenReturnEmptyList() throws Exception {

        // given

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/models")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @DisplayName("Test create car class functionality")
    public void givenCarClassDto_whenCreateCarClass_thenSuccessResponse() throws Exception {

        // given
        CreateCarModelName createCarClassRequest = new CreateCarModelName("PREMIUM");
        ModelNameResponse carClassResponse = new ModelNameResponse("PREMIUM");

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/filters/classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createCarClassRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("PREMIUM"));
    }

    @Test
    @DisplayName("Test create car class with invalid data functionality")
    public void givenInvalidCarClassDto_whenCreateCarClass_thenValidationErrorResponse() throws Exception {

        // given
        CreateCarModelName invalidRequest = new CreateCarModelName(""); // пустое имя класса

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/filters/classes")
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
    @DisplayName("Test get all car classes functionality")
    public void whenGetAllCarClasses_thenReturnListOfCarClasses() throws Exception {

        // given
        carClassRepository.save(dataUtils.getCarClassTransient("ECONOMY"));
        carClassRepository.save(dataUtils.getCarClassTransient("BUSINESS"));
        carClassRepository.save(dataUtils.getCarClassTransient("PREMIUM"));

        List<String> carClasses = List.of("ECONOMY", "BUSINESS", "PREMIUM");

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/classes")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value("BUSINESS"))
                .andExpect(jsonPath("$[0]").value("ECONOMY"))
                .andExpect(jsonPath("$[2]").value("PREMIUM"));
    }

    @Test
    @DisplayName("Test get all car classes when no classes exist functionality")
    public void whenNoCarClassesExist_thenReturnEmptyList() throws Exception {

        // given

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/classes")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }
}

