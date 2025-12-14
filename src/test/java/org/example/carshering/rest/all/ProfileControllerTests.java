package org.example.carshering.rest.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.identity.api.dto.request.CreateDocumentRequest;
import org.example.carshering.identity.api.dto.request.UpdateDocumentRequest;
import org.example.carshering.identity.api.dto.request.UpdateProfileRequest;
import org.example.carshering.identity.api.dto.response.DocumentResponse;
import org.example.carshering.identity.api.dto.response.UserResponse;
import org.example.carshering.common.exceptions.custom.NotFoundException;
import org.example.carshering.identity.api.rest.all.ProfileController;
import org.example.carshering.rest.BaseWebMvcTest;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.interfaces.ClientService;
import org.example.carshering.service.interfaces.DocumentService;
import org.example.carshering.util.WithMockClientDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;

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
        controllers = ProfileController.class
)
@Import(LocalValidatorFactoryBean.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTests extends BaseWebMvcTest {

    private final String apiUrl = "/api/profile";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private Authentication authentication;

    @MockitoBean
    private ClientDetails clientDetails;

    @Test
    @DisplayName("Test get profile functionality")
    public void whenGetProfile_thenSuccessResponse() throws Exception {

        // given
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .login("johndoe")
                .phone("+1234567890")
                .email("john@example.com")
                .build();

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(clientService.findUser(eq(1L)))
                .willReturn(userResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.login").value("johndoe"))
                .andExpect(jsonPath("$.phone").value("+1234567890"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("Test get profile with no content functionality")
    public void whenGetProfileNotFound_thenNoContentResponse() throws Exception {

        // given
        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(clientService.findUser(eq(1L)))
                .willReturn(null);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test get me functionality")
    @WithMockClientDetails(username = "johndoe")
    public void whenGetMe_thenSuccessResponse() throws Exception {

        // given
        when(clientDetails.getUsername()).thenReturn("johndoe");


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> "johndoe")
                .requestAttr("SPRING_SECURITY_CONTEXT_KEY", clientDetails));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.authorities", notNullValue()));
    }

    @Test
    @DisplayName("Test get me with no user functionality")
    public void whenGetMeWithNoUser_thenNoContentResponse() throws Exception {

        // given - user == null

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/me")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test delete profile functionality")
    public void whenDeleteProfile_thenNoContentResponse() throws Exception {

        // given
        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        doNothing().when(clientService).deleteUser(eq(1L));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        verify(clientService, times(1)).deleteUser(1L);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test change password functionality")
    public void givenPasswordRequest_whenChangePassword_thenNoContentResponse() throws Exception {

        // given
        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword123",
                "newPassword123"
        );

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        doNothing().when(clientService).changePassword(eq(1L), any(ChangePasswordRequest.class));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        verify(clientService, times(1)).changePassword(eq(1L), any(ChangePasswordRequest.class));

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test get document functionality")
    public void whenGetDocument_thenSuccessResponse() throws Exception {

        // given
        DocumentResponse documentResponse = DocumentResponse.builder()
                .id(1L)
                .documentType("Passport")
                .series("1234")
                .number("567890")
                .dateOfIssue(LocalDate.of(2020, 1, 15))
                .issuingAuthority("МВД России")
                .verified(true)
                .build();

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(documentService.findDocument(eq(1L)))
                .willReturn(documentResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentType").value("Passport"))
                .andExpect(jsonPath("$.series").value("1234"))
                .andExpect(jsonPath("$.number").value("567890"))
                .andExpect(jsonPath("$.issuingAuthority").value("МВД России"))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("Test get document with no content functionality")
    public void whenGetDocumentNotFound_thenNoContentResponse() throws Exception {

        // given
        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(documentService.findDocument(eq(1L)))
                .willReturn(null);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test create document functionality")
    public void givenDocumentDto_whenCreateDocument_thenSuccessResponse() throws Exception {

        // given
        CreateDocumentRequest request = CreateDocumentRequest.builder()
                .documentTypeId(1L)
                .series("1234")
                .number("567890")
                .dateOfIssue(LocalDate.of(2020, 1, 15))
                .issuingAuthority("МВД России")
                .build();

        DocumentResponse response = DocumentResponse.builder()
                .id(1L)
                .documentType("Passport")
                .series("1234")
                .number("567890")
                .dateOfIssue(LocalDate.of(2020, 1, 15))
                .issuingAuthority("МВД России")
                .verified(false)
                .build();

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(documentService.createDocument(any(CreateDocumentRequest.class), eq(1L)))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentType").value("Passport"))
                .andExpect(jsonPath("$.series").value("1234"))
                .andExpect(jsonPath("$.number").value("567890"))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    @DisplayName("Test create document with invalid data functionality")
    public void givenInvalidDocumentDto_whenCreateDocument_thenValidationErrorResponse() throws Exception {

        // given
        CreateDocumentRequest invalidRequest = CreateDocumentRequest.builder()
                .documentTypeId(null) // null значение
                .series(null) // null значение
                .number(null) // null значение
                .dateOfIssue(null) // null значение
                .issuingAuthority(null) // null значение
                .build();

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/document")
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
    @DisplayName("Test update document functionality")
    public void givenUpdateDocumentDto_whenUpdateDocument_thenSuccessResponse() throws Exception {

        // given
        UpdateDocumentRequest request = UpdateDocumentRequest.builder()
                .documentTypeId(1L)
                .series("5678")
                .number("123456")
                .dateOfIssue(LocalDate.of(2021, 3, 20))
                .issuingAuthority("МВД России")
                .build();

        DocumentResponse response = DocumentResponse.builder()
                .id(1L)
                .documentType("Passport")
                .series("5678")
                .number("123456")
                .dateOfIssue(LocalDate.of(2021, 3, 20))
                .issuingAuthority("МВД России")
                .verified(true)
                .build();

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(documentService.updateDocument(eq(1L), any(UpdateDocumentRequest.class)))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.series").value("5678"))
                .andExpect(jsonPath("$.number").value("123456"))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("Test update document with invalid data functionality")
    public void givenInvalidUpdateDocumentDto_whenUpdateDocument_thenValidationErrorResponse() throws Exception {

        // given
        UpdateDocumentRequest invalidRequest = UpdateDocumentRequest.builder()
                .dateOfIssue(LocalDate.now().plusDays(1)) // дата в будущем
                .build();

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/document")
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
    @DisplayName("Test update document with non-existent document functionality")
    public void givenUpdateDocumentDtoWithNonExistentDocument_whenUpdateDocument_thenErrorResponse() throws Exception {

        // given
        UpdateDocumentRequest request = UpdateDocumentRequest.builder()
                .documentTypeId(1L)
                .series("5678")
                .number("123456")
                .dateOfIssue(LocalDate.of(2021, 3, 20))
                .issuingAuthority("МВД России")
                .build();

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        given(documentService.updateDocument(eq(1L), any(UpdateDocumentRequest.class)))
                .willThrow(new NotFoundException("Document not found"));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Document not found"));
    }

    @Test
    @DisplayName("Test delete document functionality")
    public void whenDeleteDocument_thenNoContentResponse() throws Exception {

        // given
        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        doNothing().when(documentService).deleteDocument(eq(1L));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        verify(documentService, times(1)).deleteDocument(1L);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test delete document with non-existent document functionality")
    public void whenDeleteNonExistentDocument_thenErrorResponse() throws Exception {

        // given
        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        doThrow(new NotFoundException("Document not found"))
                .when(documentService).deleteDocument(eq(1L));

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(authentication));

        // then
        verify(documentService, times(1)).deleteDocument(1L);

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Document not found"));
    }

    @Test
    @DisplayName("Test update profile functionality")
    public void givenUpdateProfileDto_whenUpdateProfile_thenNoContentResponse() throws Exception {

        // given
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Jane",
                "Smith",
                "+0987654321"
        );

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(1L);
        doNothing().when(clientService).updateProfile(eq(1L), any(UpdateProfileRequest.class));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        verify(clientService, times(1)).updateProfile(eq(1L), any(UpdateProfileRequest.class));

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test update profile with non-existent user functionality")
    public void givenUpdateProfileDtoWithNonExistentUser_whenUpdateProfile_thenErrorResponse() throws Exception {

        // given
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Jane",
                "Smith",
                "+0987654321"
        );

        when(authentication.getPrincipal()).thenReturn(clientDetails);
        when(clientDetails.getId()).thenReturn(999L);
        doThrow(new NotFoundException("User not found"))
                .when(clientService).updateProfile(eq(999L), any(UpdateProfileRequest.class));

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .principal(authentication));

        // then
        verify(clientService, times(1)).updateProfile(eq(999L), any(UpdateProfileRequest.class));

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
