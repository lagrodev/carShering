package org.example.carshering.rest.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.rest.BaseWebMvcTest;
import org.example.carshering.rest.admin.AdminCarController;
import org.example.carshering.security.JwtRequestFilter;
import org.example.carshering.service.CarService;
import org.example.carshering.service.CarStateService;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = AdminCarController.class
)
@Import(LocalValidatorFactoryBean.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminCarControllerTests extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private CarStateService carStateService;

    private final DataUtils dataUtils = new DataUtils();

    @Test
    @DisplayName("Test create car functionality")
    public void givenCarDto_whenCreateCar_thenSuccessResponse() throws Exception {

        // given
        CreateCarRequest createCarRequest = dataUtils.createCarRequestTransient();
        CarDetailResponse carDetailResponse = DataUtils.carDetailResponsePersisted();

        given(carService.createCar(any(CreateCarRequest.class))).willReturn(carDetailResponse);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/admin/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createCarRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.gosNumber").value(createCarRequest.gosNumber()))
                .andExpect(jsonPath("$.vin").value(createCarRequest.vin()))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))


        ;
    }
}
