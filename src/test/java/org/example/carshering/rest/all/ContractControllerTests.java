package org.example.carshering.rest.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.request.create.CreateContractRequest;
import org.example.carshering.dto.request.update.UpdateContractRequest;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.exceptions.custom.CarNotFoundException;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.exceptions.custom.UnauthorizedContractAccessException;
import org.example.carshering.rest.BaseWebMvcTest;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.interfaces.ContractService;
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
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
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
        controllers = ContractController.class
)
@Import(LocalValidatorFactoryBean.class)
@AutoConfigureMockMvc(addFilters = false)
public class ContractControllerTests extends BaseWebMvcTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/contracts";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContractService contractService;

    @MockitoBean
    private Authentication authentication;

    @MockitoBean
    private ClientDetails clientDetails;

    @Test
    @DisplayName("Test create contract functionality")
    public void givenContractDto_whenCreateContract_thenSuccessResponse() throws Exception {

        // given
        CreateContractRequest request = new CreateContractRequest(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10)
        );
        ContractResponse response = DataUtils.contractResponsePersisted();

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(contractService.createContract(eq(1L), any(CreateContractRequest.class)))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.totalCost").value(1000.0))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    @DisplayName("Test create contract with invalid data functionality")
    public void givenInvalidContractDto_whenCreateContract_thenValidationErrorResponse() throws Exception {

        // given
        CreateContractRequest invalidRequest = new CreateContractRequest(
                null, // carId — null
                LocalDateTime.now().minusDays(1), // дата в прошлом
                LocalDateTime.now().minusDays(5) // дата в прошлом
        );

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(invalidRequest))
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("Test create contract with non-existent car functionality")
    public void givenContractDtoWithNonExistentCar_whenCreateContract_thenErrorResponse() throws Exception {

        // given
        CreateContractRequest request = new CreateContractRequest(
                999L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10)
        );

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(contractService.createContract(eq(1L), any(CreateContractRequest.class)))
                .willThrow(new CarNotFoundException("Car not found"));

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("CarNotFoundException")))
                .andExpect(jsonPath("$.message").value("Car not found"));
    }

    @Test
    @DisplayName("Test get all contracts functionality")
    public void whenGetAllContracts_thenReturnPagedContracts() throws Exception {

        // given
        ContractResponse contract1 = DataUtils.contractResponsePersisted();
        ContractResponse contract2 = DataUtils.contractResponseConfirmed();

        Page<ContractResponse> page = new PageImpl<>(List.of(contract1, contract2));

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(contractService.getAllClientContracts(any(Pageable.class), eq(1L)))
                .willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].state").value("PENDING"))
                .andExpect(jsonPath("$.content[1].id").value(1L))
                .andExpect(jsonPath("$.content[1].state").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Test get contract by id functionality")
    public void givenContractId_whenGetContract_thenSuccessResponse() throws Exception {

        // given
        ContractResponse response = DataUtils.contractResponsePersisted();

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(contractService.findContract(eq(1L), eq(1L)))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.state").value("PENDING"))
                .andExpect(jsonPath("$.totalCost").value(1000.0));
    }

    @Test
    @DisplayName("Test get contract by incorrect id functionality")
    public void givenIncorrectContractId_whenGetContract_thenErrorResponse() throws Exception {

        // given
        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(contractService.findContract(eq(999L), eq(1L)))
                .willThrow(new NotFoundException("Contract not found"));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/999")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test get contract with non-numeric contractId functionality")
    public void givenNonNumericContractId_whenGetContract_thenBadRequestResponse() throws Exception {

        // given
        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/invalid-id")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'contractId': 'invalid-id'")));
    }

    @Test
    @DisplayName("Test cancel contract functionality")
    public void givenContractId_whenCancelContract_thenNoContentResponse() throws Exception {

        // given
        Long contractId = 1L;

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        doNothing().when(contractService).cancelContract(eq(1L), eq(contractId));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{contractId}/cancel", contractId)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        verify(contractService, times(1)).cancelContract(1L, contractId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test cancel contract by incorrect id functionality")
    public void givenIncorrectContractId_whenCancelContract_thenErrorResponse() throws Exception {

        // given
        Long contractId = 999L;

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        doThrow(new NotFoundException("Contract not found"))
                .when(contractService).cancelContract(eq(1L), eq(contractId));
        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{contractId}/cancel", contractId)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        verify(contractService, times(1)).cancelContract(1L, contractId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test cancel contract by another user functionality")
    public void givenContractIdOfAnotherUser_whenCancelContract_thenForbiddenResponse() throws Exception {

        // given
        Long contractId = 1L;

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(2L);
        doThrow(new UnauthorizedContractAccessException("Access denied"))
                .when(contractService).cancelContract(eq(2L), eq(contractId));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{contractId}/cancel", contractId)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        verify(contractService, times(1)).cancelContract(2L, contractId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("UnauthorizedContractAccessException")))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("Test update contract functionality")
    public void givenUpdateContractDto_whenUpdateContract_thenSuccessResponse() throws Exception {

        // given
        UpdateContractRequest request = new UpdateContractRequest(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(12)
        );
        ContractResponse response = DataUtils.contractResponsePersisted();

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(contractService.updateContract(eq(1L), eq(1L), any(UpdateContractRequest.class)))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    @DisplayName("Test update contract with invalid data functionality")
    public void givenInvalidUpdateContractDto_whenUpdateContract_thenValidationErrorResponse() throws Exception {

        // given
        UpdateContractRequest invalidRequest = new UpdateContractRequest(
                LocalDateTime.now().minusDays(1), // дата в прошлом
                null // null дата
        );

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(invalidRequest))
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("Test update contract with incorrect id functionality")
    public void givenUpdateContractDtoWithIncorrectId_whenUpdateContract_thenErrorResponse() throws Exception {

        // given
        UpdateContractRequest request = new UpdateContractRequest(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(12)
        );

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(contractService.updateContract(eq(1L), eq(999L), any(UpdateContractRequest.class)))
                .willThrow(new NotFoundException("Contract not found"));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test update contract by another user functionality")
    public void givenUpdateContractDtoByAnotherUser_whenUpdateContract_thenForbiddenResponse() throws Exception {

        // given
        UpdateContractRequest request = new UpdateContractRequest(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(12)
        );

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(2L);
        given(contractService.updateContract(eq(2L), eq(1L), any(UpdateContractRequest.class)))
                .willThrow(new UnauthorizedContractAccessException("Access denied"));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("UnauthorizedContractAccessException")))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }
}

