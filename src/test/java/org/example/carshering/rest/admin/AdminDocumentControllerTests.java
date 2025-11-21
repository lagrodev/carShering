package org.example.carshering.rest.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.rest.BaseWebMvcTest;
import org.example.carshering.service.interfaces.DocumentService;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminDocumentController.class
)
@Import(LocalValidatorFactoryBean.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminDocumentControllerTests extends BaseWebMvcTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/admin/documents";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentService documentService;

    @Test
    @DisplayName("Test get all documents with only unverified functionality")
    public void givenOnlyUnverifiedTrue_whenGetAllDocuments_thenSuccessResponse() throws Exception {

        // given
        DocumentResponse doc1 = DataUtils.documentResponsePersisted("Паспорт", "1234", "567890", "УФМС", false);
        DocumentResponse doc2 = DataUtils.documentResponsePersisted("Водительское удостоверение", "5678", "123456", "ГИБДД", false);
        List<DocumentResponse> documents = Arrays.asList(doc1, doc2);

        given(documentService.getAllDocuments(eq(true), any(Pageable.class))).willReturn(new PageImpl<>(documents));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("onlyUnverified", "true")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].series").value("1234"))
                .andExpect(jsonPath("$.content[0].number").value("567890"))
                .andExpect(jsonPath("$.content[0].verified").value(false))
                .andExpect(jsonPath("$.content[1].series").value("5678"))
                .andExpect(jsonPath("$.content[1].number").value("123456"))
                .andExpect(jsonPath("$.content[1].verified").value(false));
    }

    @Test
    @DisplayName("Test get all documents with only unverified false functionality")
    public void givenOnlyUnverifiedFalse_whenGetAllDocuments_thenSuccessResponse() throws Exception {

        // given
        DocumentResponse doc1 = DataUtils.documentResponsePersisted("Паспорт", "1234", "567890", "УФМС", true);
        DocumentResponse doc2 = DataUtils.documentResponsePersisted("Паспорт", "5678", "123456", "УФМС", false);
        List<DocumentResponse> documents = Arrays.asList(doc1, doc2);

        given(documentService.getAllDocuments(eq(false), any(Pageable.class))).willReturn(new PageImpl<>(documents));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("onlyUnverified", "false")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].verified").value(true))
                .andExpect(jsonPath("$.content[1].verified").value(false));
    }

    @Test
    @DisplayName("Test get all documents with default parameter functionality")
    public void givenNoParameter_whenGetAllDocuments_thenSuccessResponseWithDefaultTrue() throws Exception {

        // given
        DocumentResponse doc1 = DataUtils.documentResponsePersisted("Паспорт", "1234", "567890", "УФМС", false);
        List<DocumentResponse> documents = Collections.singletonList(doc1);

        given(documentService.getAllDocuments(eq(true), any(Pageable.class))).willReturn(new PageImpl<>(documents));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].verified").value(false));
    }

    @Test
    @DisplayName("Test get all documents empty list functionality")
    public void givenNoDocuments_whenGetAllDocuments_thenEmptyListResponse() throws Exception {

        // given
        given(documentService.getAllDocuments(anyBoolean(), any(Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Test verify document functionality")
    public void givenDocumentId_whenVerifyDocument_thenSuccessResponse() throws Exception {

        // given
        Long documentId = 1L;

        doNothing().when(documentService).verifyDocument(eq(documentId));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/{documentId}/verify", documentId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(documentService, times(1)).verifyDocument(documentId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test verify document with incorrect id functionality")
    public void givenIncorrectDocumentId_whenVerifyDocument_thenErrorResponse() throws Exception {

        // given
        Long documentId = 999L;

        doThrow(new NotFoundException("Document not found"))
                .when(documentService).verifyDocument(eq(documentId));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/{documentId}/verify", documentId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        verify(documentService, times(1)).verifyDocument(documentId);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Document not found"));
    }

    @Test
    @DisplayName("Test verify document with non-numeric documentId functionality")
    public void givenNonNumericDocumentId_whenVerifyDocument_thenBadRequestResponse() throws Exception {

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/invalid-id/verify")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'documentId': 'invalid-id'")));
    }
}
