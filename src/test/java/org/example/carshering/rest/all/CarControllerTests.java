package org.example.carshering.rest.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarListItemResponse;
import org.example.carshering.exceptions.custom.CarNotFoundException;
import org.example.carshering.rest.BaseWebMvcTest;
import org.example.carshering.service.*;
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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CarController.class
)
@Import(LocalValidatorFactoryBean.class)
@AutoConfigureMockMvc(addFilters = false)
public class CarControllerTests extends BaseWebMvcTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/car";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private CarModelService carModelService;

    @MockitoBean
    private CarBrandService carBrandService;

    @MockitoBean
    private CarModelNameService carModelNameService;

    @MockitoBean
    private CarClassService carClassService;

    @Test
    @DisplayName("Test get catalogue functionality")
    public void givenNoFilters_whenGetCatalogue_thenSuccessResponse() throws Exception {

        // given
        CarListItemResponse car1 = CarListItemResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .rent(50.0)
                .status("AVAILABLE")
                .build();

        CarListItemResponse car2 = CarListItemResponse.builder()
                .id(2L)
                .brand("Honda")
                .model("Civic")
                .carClass("Economy")
                .yearOfIssue(2021)
                .rent(45.0)
                .status("AVAILABLE")
                .build();

        Page<CarListItemResponse> page = new PageImpl<>(Arrays.asList(car1, car2));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/catalogue")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].model").value("Camry"))
                .andExpect(jsonPath("$.content[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].brand").value("Honda"));
    }

    @Test
    @DisplayName("Test get catalogue with brand filter functionality")
    public void givenBrandFilter_whenGetCatalogue_thenFilteredResponse() throws Exception {

        // given
        CarListItemResponse car1 = CarListItemResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .rent(50.0)
                .status("AVAILABLE")
                .build();

        Page<CarListItemResponse> page = new PageImpl<>(List.of(car1));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

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
        CarListItemResponse car1 = CarListItemResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .rent(50.0)
                .status("AVAILABLE")
                .build();

        Page<CarListItemResponse> page = new PageImpl<>(List.of(car1));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

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
        CarListItemResponse car1 = CarListItemResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .rent(50.0)
                .status("AVAILABLE")
                .build();

        Page<CarListItemResponse> page = new PageImpl<>(List.of(car1));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

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
        CarListItemResponse car1 = CarListItemResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .rent(50.0)
                .status("AVAILABLE")
                .build();

        Page<CarListItemResponse> page = new PageImpl<>(List.of(car1));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

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
        CarListItemResponse car1 = CarListItemResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .rent(50.0)
                .status("AVAILABLE")
                .build();

        CarListItemResponse car2 = CarListItemResponse.builder()
                .id(2L)
                .brand("Honda")
                .model("Civic")
                .carClass("Economy")
                .yearOfIssue(2021)
                .rent(45.0)
                .status("AVAILABLE")
                .build();

        Page<CarListItemResponse> page = new PageImpl<>(Arrays.asList(car1, car2));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

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
        CarListItemResponse car1 = CarListItemResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .rent(50.0)
                .status("AVAILABLE")
                .build();

        Page<CarListItemResponse> page = new PageImpl<>(List.of(car1));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

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
                .andExpect(jsonPath("$.pageable", notNullValue()));
    }

    @Test
    @DisplayName("Test get car by id functionality")
    public void givenCarId_whenGetCar_thenSuccessResponse() throws Exception {

        // given
        CarDetailResponse carDetailResponse = CarDetailResponse.builder()
                .id(1L)
                .modelId(1L)
                .brand("Toyota")
                .model("Camry")
                .bodyType("SEDAN")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .gosNumber("A123BC777")
                .vin("1HGBH41JXMN109186")
                .status("AVAILABLE")
                .rent(50.0)
                .build();

        given(carService.getValidCarById(1L)).willReturn(carDetailResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.bodyType").value("SEDAN"))
                .andExpect(jsonPath("$.carClass").value("Comfort"))
                .andExpect(jsonPath("$.yearOfIssue").value(2020))
                .andExpect(jsonPath("$.gosNumber").value("A123BC777"))
                .andExpect(jsonPath("$.vin").value("1HGBH41JXMN109186"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.rent").value(50.0));
    }

    @Test
    @DisplayName("Test get car by incorrect id functionality")
    public void givenIncorrectCarId_whenGetCar_thenErrorResponse() throws Exception {

        // given
        given(carService.getValidCarById(999L))
                .willThrow(new CarNotFoundException("Car not found"));

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
        List<String> brands = Arrays.asList("Toyota", "Honda", "BMW", "Mercedes");

        given(carBrandService.findAllBrands()).willReturn(brands);

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
        given(carBrandService.findAllBrands()).willReturn(List.of());

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
        List<String> models = Arrays.asList("Camry", "Civic", "X5", "E-Class");

        given(carModelNameService.findAllModels()).willReturn(models);

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
        List<String> classes = Arrays.asList("Economy", "Comfort", "Business", "Premium");

        given(carClassService.findAllClasses()).willReturn(classes);

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
        List<String> bodyTypes = Arrays.asList("SEDAN", "SUV", "HATCHBACK", "COUPE");

        given(carModelService.findAllBodyTypes()).willReturn(bodyTypes);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/filters/body-types")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value("SEDAN"))
                .andExpect(jsonPath("$[1]").value("SUV"))
                .andExpect(jsonPath("$[2]").value("HATCHBACK"))
                .andExpect(jsonPath("$[3]").value("COUPE"));
    }

    @Test
    @DisplayName("Test get body types when empty functionality")
    public void givenNoBodyTypes_whenGetBodyTypes_thenEmptyListResponse() throws Exception {

        // given
        given(carModelService.findAllBodyTypes()).willReturn(List.of());

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
        CarListItemResponse car1 = CarListItemResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .rent(50.0)
                .status("AVAILABLE")
                .build();

        Page<CarListItemResponse> page = new PageImpl<>(List.of(car1));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

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
        CarListItemResponse car1 = CarListItemResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .rent(50.0)
                .status("AVAILABLE")
                .build();

        Page<CarListItemResponse> page = new PageImpl<>(List.of(car1));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

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
        CarListItemResponse car1 = CarListItemResponse.builder()
                .id(1L)
                .brand("Toyota")
                .model("Camry")
                .carClass("Comfort")
                .yearOfIssue(2020)
                .rent(50.0)
                .status("AVAILABLE")
                .build();

        Page<CarListItemResponse> page = new PageImpl<>(List.of(car1));

        given(carService.getAllCars(any(Pageable.class), any())).willReturn(page);

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

