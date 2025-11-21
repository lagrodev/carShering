package org.example.carshering.rest.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.request.create.CreateCarModelRequest;
import org.example.carshering.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.dto.request.update.UpdateCarModelRequest;
import org.example.carshering.dto.response.BrandModelResponse;
import org.example.carshering.dto.response.CarModelResponse;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.exceptions.custom.AlreadyExistsException;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.rest.BaseWebMvcTest;
import org.example.carshering.service.interfaces.CarBrandService;
import org.example.carshering.service.interfaces.CarClassService;
import org.example.carshering.service.interfaces.CarModelNameService;
import org.example.carshering.service.interfaces.CarModelService;
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
        controllers = AdminModelDetailsController.class
)
@Import(LocalValidatorFactoryBean.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminModelDetailsControllerTests extends BaseWebMvcTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/admin";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarModelService carModelService;

    @MockitoBean
    private CarBrandService carBrandService;

    @MockitoBean
    private CarModelNameService carModelNameService;

    @MockitoBean
    private CarClassService carClassService;


    @Test
    @DisplayName("Test get all models without filters functionality")
    public void whenGetAllModelsWithoutFilters_thenReturnPagedModels() throws Exception {
        // given
        CarModelResponse model1 = new CarModelResponse(1L, "Toyota", "Camry", "SEDAN", "BUSINESS", false);
        CarModelResponse model2 = new CarModelResponse(2L, "BMW", "X5", "SUV", "PREMIUM", false);

        Page<CarModelResponse> page = new PageImpl<>(List.of(model1, model2));

        given(carModelService.getAllModelsIncludingDeleted(any(), any(Pageable.class))).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/models")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].modelId").value(1L))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].model").value("Camry"))
                .andExpect(jsonPath("$.content[0].bodyType").value("SEDAN"))
                .andExpect(jsonPath("$.content[0].carClass").value("BUSINESS"))
                .andExpect(jsonPath("$.content[0].isDeleted").value(false))
                .andExpect(jsonPath("$.content[1].brand").value("BMW"));
    }

    @Test
    @DisplayName("Test get all models with filters functionality")
    public void givenFilters_whenGetModels_thenReturnFilteredPagedModels() throws Exception {
        // given
        CarModelResponse model = new CarModelResponse(3L, "Mercedes", "E-Class", "SEDAN", "PREMIUM", false);
        Page<CarModelResponse> page = new PageImpl<>(List.of(model));

        given(carModelService.getAllModelsIncludingDeleted(any(), any(Pageable.class))).willReturn(page);

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
                .andExpect(jsonPath("$.content[0].modelId").value(3L))
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

        given(carModelService.getAllModelsIncludingDeleted(any(), any(Pageable.class))).willReturn(emptyPage);

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

    // --- ТЕСТЫ ДЛЯ ПОЛУЧЕНИЯ МОДЕЛИ ПО ID ---

    @Test
    @DisplayName("Test get model by id functionality")
    public void givenId_whenGetModelById_thenSuccessResponse() throws Exception {
        // given
        CarModelResponse modelResponse = new CarModelResponse(1L, "Toyota", "Camry", "SEDAN", "BUSINESS", false);

        given(carModelService.getModelById(1L)).willReturn(modelResponse);

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
        given(carModelService.getModelById(999L))
                .willThrow(new NotFoundException("Car model not found"));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/models/999")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Car model not found"));
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

    // --- ТЕСТЫ ДЛЯ СОЗДАНИЯ МОДЕЛИ ---

    @Test
    @DisplayName("Test create model functionality")
    public void givenCarModelDto_whenCreateModel_thenSuccessResponse() throws Exception {
        // given
        CreateCarModelRequest createRequest = new CreateCarModelRequest("Toyota", "Camry", "SEDAN", "BUSINESS");
        CarModelResponse modelResponse = new CarModelResponse(1L, "Toyota", "Camry", "SEDAN", "BUSINESS", false);

        given(carModelService.createModel(any(CreateCarModelRequest.class))).willReturn(modelResponse);

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
        CreateCarModelRequest createRequest = new CreateCarModelRequest("Toyota", "Camry", "SEDAN", "BUSINESS");

        given(carModelService.createModel(any(CreateCarModelRequest.class)))
                .willThrow(new AlreadyExistsException("Car model already exists"));

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
                .andExpect(jsonPath("$.message").value("Car model already exists"));
    }

    // --- ТЕСТЫ ДЛЯ ОБНОВЛЕНИЯ МОДЕЛИ ---

    @Test
    @DisplayName("Test update model functionality")
    public void givenCarModelDto_whenUpdateModel_thenSuccessResponse() throws Exception {
        // given
        UpdateCarModelRequest updateRequest = new UpdateCarModelRequest("Toyota", "Camry", "SEDAN", "PREMIUM");
        CarModelResponse modelResponse = new CarModelResponse(1L, "Toyota", "Camry", "SEDAN", "PREMIUM", false);

        given(carModelService.updateModel(eq(1L), any(UpdateCarModelRequest.class)))
                .willReturn(modelResponse);

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

        given(carModelService.updateModel(eq(999L), any(UpdateCarModelRequest.class)))
                .willThrow(new NotFoundException("Car model not found"));

        // when
        ResultActions resultActions = mockMvc.perform(put(apiUrl + "/models/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Car model not found"));
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

    // --- ТЕСТЫ ДЛЯ УДАЛЕНИЯ МОДЕЛИ ---

    @Test
    @DisplayName("Test delete model by id functionality")
    public void givenModelId_whenDeleteModel_thenNoContentResponse() throws Exception {
        // given
        Long modelId = 1L;

        doNothing().when(carModelService).deleteModel(eq(modelId));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/models/{modelId}", modelId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(carModelService, times(1)).deleteModel(modelId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test delete model by incorrect id functionality")
    public void givenIncorrectModelId_whenDeleteModel_thenErrorResponse() throws Exception {
        // given
        Long modelId = 999L;

        doThrow(new NotFoundException("Car model not found"))
                .when(carModelService).deleteModel(eq(modelId));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/models/{modelId}", modelId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(carModelService, times(1)).deleteModel(modelId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Car model not found"));
    }

    // --- ТЕСТЫ ДЛЯ РАБОТЫ С БРЕНДАМИ ---

    @Test
    @DisplayName("Test create brand functionality")
    public void givenBrandDto_whenCreateBrand_thenSuccessResponse() throws Exception {
        // given
        CreateCarModelsBrand createBrandRequest = new CreateCarModelsBrand("Mercedes");
        BrandModelResponse brandResponse = new BrandModelResponse("Mercedes");

        given(carBrandService.createBrands(any(CreateCarModelsBrand.class))).willReturn(brandResponse);

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
        List<String> brands = List.of("Toyota", "BMW", "Mercedes");

        given(carBrandService.findAllBrands()).willReturn(brands);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/brands")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Toyota"))
                .andExpect(jsonPath("$[1]").value("BMW"))
                .andExpect(jsonPath("$[2]").value("Mercedes"));
    }

    @Test
    @DisplayName("Test get all brands when no brands exist functionality")
    public void whenNoBrandsExist_thenReturnEmptyList() throws Exception {
        // given
        given(carBrandService.findAllBrands()).willReturn(List.of());

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/brands")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    // --- ТЕСТЫ ДЛЯ РАБОТЫ С НАЗВАНИЯМИ МОДЕЛЕЙ ---

    @Test
    @DisplayName("Test create model name functionality")
    public void givenModelNameDto_whenCreateModelName_thenSuccessResponse() throws Exception {
        // given
        CreateCarModelName createModelNameRequest = new CreateCarModelName("Camry");
        ModelNameResponse modelNameResponse = new ModelNameResponse("Camry");

        given(carModelNameService.createModelName(any(CreateCarModelName.class))).willReturn(modelNameResponse);

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
        List<String> modelNames = List.of("Camry", "Corolla", "X5", "3 Series");

        given(carModelNameService.findAllModels()).willReturn(modelNames);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/models")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Camry"))
                .andExpect(jsonPath("$[1]").value("Corolla"))
                .andExpect(jsonPath("$[2]").value("X5"))
                .andExpect(jsonPath("$[3]").value("3 Series"));
    }

    @Test
    @DisplayName("Test get all model names when no models exist functionality")
    public void whenNoModelNamesExist_thenReturnEmptyList() throws Exception {
        // given
        given(carModelNameService.findAllModels()).willReturn(List.of());

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/models")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    // --- ТЕСТЫ ДЛЯ РАБОТЫ С КЛАССАМИ АВТОМОБИЛЕЙ ---

    @Test
    @DisplayName("Test create car class functionality")
    public void givenCarClassDto_whenCreateCarClass_thenSuccessResponse() throws Exception {
        // given
        CreateCarModelName createCarClassRequest = new CreateCarModelName("PREMIUM");
        ModelNameResponse carClassResponse = new ModelNameResponse("PREMIUM");

        given(carClassService.createCarClass(any(CreateCarModelName.class))).willReturn(carClassResponse);

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
        List<String> carClasses = List.of("ECONOMY", "BUSINESS", "PREMIUM");

        given(carClassService.findAllClasses()).willReturn(carClasses);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/classes")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("ECONOMY"))
                .andExpect(jsonPath("$[1]").value("BUSINESS"))
                .andExpect(jsonPath("$[2]").value("PREMIUM"));
    }

    @Test
    @DisplayName("Test get all car classes when no classes exist functionality")
    public void whenNoCarClassesExist_thenReturnEmptyList() throws Exception {
        // given
        given(carClassService.findAllClasses()).willReturn(List.of());

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

