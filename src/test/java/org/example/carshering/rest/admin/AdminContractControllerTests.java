package org.example.carshering.rest.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.exceptions.custom.EntityNotFoundException;
import org.example.carshering.exceptions.custom.InvalidContractStateException;
import org.example.carshering.exceptions.custom.InvalidContractCancellationStateException;
import org.example.carshering.rest.BaseWebMvcTest;
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
        controllers = AdminContractController.class
)
@Import(LocalValidatorFactoryBean.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminContractControllerTests extends BaseWebMvcTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/admin/contracts";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ContractService contractService;

    @Test
    @DisplayName("Test confirm contract functionality")
    public void givenContractId_whenConfirmContract_thenSuccessResponse() throws Exception {

        // given
        Long contractId = 1L;
        ContractResponse contractResponse = DataUtils.contractResponseConfirmed();

        given(contractService.confirmContract(eq(contractId))).willReturn(contractResponse);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/{contractId}/confirm", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("CONFIRMED"))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"));
    }

    @Test
    @DisplayName("Test confirm contract with incorrect id functionality")
    public void givenIncorrectContractId_whenConfirmContract_thenErrorResponse() throws Exception {

        // given
        Long contractId = 999L;

        given(contractService.confirmContract(eq(contractId)))
                .willThrow(new EntityNotFoundException("Contract not found"));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/{contractId}/confirm", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("EntityNotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test confirm already confirmed contract functionality")
    public void givenAlreadyConfirmedContract_whenConfirmContract_thenErrorResponse() throws Exception {

        // given
        Long contractId = 1L;

        given(contractService.confirmContract(eq(contractId)))
                .willThrow(new InvalidContractStateException("Contract already confirmed"));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/{contractId}/confirm", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("InvalidContractStateException")))
                .andExpect(jsonPath("$.message").value("Contract already confirmed"));
    }

    @Test
    @DisplayName("Test get all contracts without filters functionality")
    public void whenGetAllContractsWithoutFilters_thenReturnPagedContracts() throws Exception {

        // given
        ContractResponse contract1 = DataUtils.contractResponsePersisted();
        ContractResponse contract2 = DataUtils.contractResponseCancelled();

        Page<ContractResponse> page = new PageImpl<>(List.of(contract1, contract2));

        given(contractService.getAllContracts(any(Pageable.class), any())).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].model").value("Camry"))
                .andExpect(jsonPath("$.content[0].state").value("PENDING"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].brand").value("BMW"))
                .andExpect(jsonPath("$.content[1].state").value("CANCELLED"));
    }

    @Test
    @DisplayName("Test get all contracts with status filter functionality")
    public void givenStatusFilter_whenGetContracts_thenReturnFilteredContracts() throws Exception {

        // given
        ContractResponse contract = DataUtils.contractResponseConfirmed();
        Page<ContractResponse> page = new PageImpl<>(List.of(contract));

        given(contractService.getAllContracts(any(Pageable.class), any())).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("status", "CONFIRMED")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].state").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Test get all contracts with user id filter functionality")
    public void givenUserIdFilter_whenGetContracts_thenReturnFilteredContracts() throws Exception {

        // given
        ContractResponse contract = DataUtils.contractResponsePersisted();
        Page<ContractResponse> page = new PageImpl<>(List.of(contract));

        given(contractService.getAllContracts(any(Pageable.class), any())).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("idUser", "1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].lastName").value("Ivanov"));
    }

    @Test
    @DisplayName("Test get all contracts with car id filter functionality")
    public void givenCarIdFilter_whenGetContracts_thenReturnFilteredContracts() throws Exception {

        // given
        ContractResponse contract = DataUtils.contractResponsePersisted();
        Page<ContractResponse> page = new PageImpl<>(List.of(contract));

        given(contractService.getAllContracts(any(Pageable.class), any())).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("idCar", "5")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].vin").value("VIN123456"));
    }

    @Test
    @DisplayName("Test get all contracts with multiple filters functionality")
    public void givenMultipleFilters_whenGetContracts_thenReturnFilteredContracts() throws Exception {

        // given
        ContractResponse contract = DataUtils.contractResponsePersisted();
        Page<ContractResponse> page = new PageImpl<>(List.of(contract));

        given(contractService.getAllContracts(any(Pageable.class), any())).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("status", "PENDING")
                .param("brand", "Toyota")
                .param("body_type", "SEDAN")
                .param("car_class", "ECONOMY")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].bodyType").value("SEDAN"))
                .andExpect(jsonPath("$.content[0].carClass").value("ECONOMY"));
    }

    @Test
    @DisplayName("Test get contract by id functionality")
    public void givenContractId_whenGetById_thenSuccessResponse() throws Exception {

        // given
        Long contractId = 1L;
        ContractResponse contractResponse = DataUtils.contractResponsePersisted();

        given(contractService.getContractById(eq(contractId))).willReturn(contractResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/{contractId}", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.totalCost").value(1000.0))
                .andExpect(jsonPath("$.vin").value("VIN123456"));
    }

    @Test
    @DisplayName("Test get contract by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenErrorResponse() throws Exception {

        // given
        Long contractId = 999L;

        given(contractService.getContractById(eq(contractId)))
                .willThrow(new EntityNotFoundException("Contract not found"));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/{contractId}", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("EntityNotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test get contract with non-numeric contractId functionality")
    public void givenNonNumericContractId_whenGetContract_thenBadRequestResponse() throws Exception {

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/invalid-id")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'contractId': 'invalid-id'")));
    }

    @Test
    @DisplayName("Test cancel contract by admin functionality")
    public void givenContractId_whenCancelContract_thenSuccessResponse() throws Exception {

        // given
        Long contractId = 1L;

        doNothing().when(contractService).cancelContractByAdmin(eq(contractId));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{contractId}/cancel", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(contractService, times(1)).cancelContractByAdmin(contractId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test cancel contract by admin with incorrect id functionality")
    public void givenIncorrectContractId_whenCancelContract_thenErrorResponse() throws Exception {

        // given
        Long contractId = 999L;

        doThrow(new EntityNotFoundException("Contract not found"))
                .when(contractService).cancelContractByAdmin(eq(contractId));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{contractId}/cancel", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(contractService, times(1)).cancelContractByAdmin(contractId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("EntityNotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test confirm cancellation by admin functionality")
    public void givenContractId_whenConfirmCancellation_thenSuccessResponse() throws Exception {

        // given
        Long contractId = 1L;

        doNothing().when(contractService).confirmCancellationByAdmin(eq(contractId));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/contracts/{id}/confirm-cancellation", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(contractService, times(1)).confirmCancellationByAdmin(contractId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test confirm cancellation with incorrect id functionality")
    public void givenIncorrectContractId_whenConfirmCancellation_thenErrorResponse() throws Exception {

        // given
        Long contractId = 999L;

        doThrow(new EntityNotFoundException("Contract not found"))
                .when(contractService).confirmCancellationByAdmin(eq(contractId));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/contracts/{id}/confirm-cancellation", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(contractService, times(1)).confirmCancellationByAdmin(contractId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("EntityNotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test confirm cancellation with invalid state functionality")
    public void givenInvalidContractState_whenConfirmCancellation_thenErrorResponse() throws Exception {

        // given
        Long contractId = 1L;

        doThrow(new InvalidContractCancellationStateException("Contract is not in cancellation pending state"))
                .when(contractService).confirmCancellationByAdmin(eq(contractId));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/contracts/{id}/confirm-cancellation", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(contractService, times(1)).confirmCancellationByAdmin(contractId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("InvalidContractCancellationStateException")))
                .andExpect(jsonPath("$.message").value("Contract is not in cancellation pending state"));
    }

    @Test
    @DisplayName("Test get all contracts with pagination functionality")
    public void givenPageableParams_whenGetAllContracts_thenReturnPagedResponse() throws Exception {

        // given
        ContractResponse contract1 = DataUtils.contractResponsePersisted();
        ContractResponse contract2 = DataUtils.contractResponseCancelled();

        Page<ContractResponse> page = new PageImpl<>(
                List.of(contract1, contract2),
                org.springframework.data.domain.PageRequest.of(0, 20),
                2
        );

        given(contractService.getAllContracts(any(Pageable.class), any())).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("Test get all contracts returns empty page functionality")
    public void whenGetAllContractsWithNoData_thenReturnEmptyPage() throws Exception {

        // given
        Page<ContractResponse> emptyPage = new PageImpl<>(List.of());

        given(contractService.getAllContracts(any(Pageable.class), any())).willReturn(emptyPage);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}

