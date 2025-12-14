package org.example.carshering.it.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.fleet.infrastructure.persistence.repository.*;
import org.example.carshering.identity.api.dto.request.CreateDocumentRequest;
import org.example.carshering.identity.api.dto.request.UpdateDocumentRequest;
import org.example.carshering.identity.api.dto.request.UpdateProfileRequest;
import org.example.carshering.identity.infrastructure.persistence.entity.Client;
import org.example.carshering.identity.infrastructure.persistence.entity.Document;
import org.example.carshering.identity.infrastructure.persistence.entity.DocumentType;
import org.example.carshering.identity.infrastructure.persistence.entity.Role;
import org.example.carshering.identity.infrastructure.persistence.repository.ClientRepository;
import org.example.carshering.identity.infrastructure.persistence.repository.DocumentRepository;
import org.example.carshering.identity.infrastructure.persistence.repository.DocumentTypeRepository;
import org.example.carshering.identity.infrastructure.persistence.repository.RoleRepository;
import org.example.carshering.it.BaseWebIntegrateTest;
import org.example.carshering.rental.infrastructure.persistence.repository.ContractRepository;
import org.example.carshering.repository.RentalStateRepository;
import org.example.carshering.util.WithMockClientDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProfileControllerIntegrationTests extends BaseWebIntegrateTest {
    private final String apiUrl = "/api/profile";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelNameRepository modelNameRepository;

    @Autowired
    private CarClassRepository carClassRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarStateRepository carStateRepository;

    @Autowired
    private RentalStateRepository rentalStateRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentTypeRepository documentTypeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void resetSequences() {
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.document_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.doctype_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.contract_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.client_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.role_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_model_id_model_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_state_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.brands_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.models_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_classes_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.rental_state_id_seq RESTART WITH 1");
    }

    @BeforeEach
    @Transactional
    public void setup() {
        documentRepository.deleteAll();
        documentTypeRepository.deleteAll();
        contractRepository.deleteAll();
        carRepository.deleteAll();
        carStateRepository.deleteAll();
        carModelRepository.deleteAll();
        carClassRepository.deleteAll();
        modelNameRepository.deleteAll();
        brandRepository.deleteAll();
        clientRepository.deleteAll();
        roleRepository.deleteAll();
        rentalStateRepository.deleteAll();
    }

    @BeforeEach
    void setUp() throws ServletException, IOException {
        // Настройте мок фильтра, чтобы он просто пропускал запросы
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtRequestFilter).doFilter(any(), any(), any());
    }

    private HashMap<String, Object> createAndSaveClient() {
        return createAndSaveClient(
                "John", "Doe", "johndoe", "john@example.com", "+1234567890"
        );
    }

    private HashMap<String, Object> createAndSaveClient(
            String firstName, String lastName,
            String login, String email,
            String phone
    ) {
        HashMap<String, Object> hashMap = new HashMap<>();

        Role role = roleRepository.save(Role.builder().name("USER").build());

        Client client = clientRepository.save(Client.builder()
                .firstName(firstName)
                .lastName(lastName)
                .login(login)
                .email(email)
                .role(role)
                .phone(phone)
                .password(passwordEncoder.encode("password"))
                .build()
        );
        var dt = documentTypeRepository.save(DocumentType.builder().name("PASSPORT").build());


        hashMap.put("Role", role);
        hashMap.put("DocumentType", dt);
        hashMap.put("Client", client);
        return hashMap;
    }


    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test get profile functionality")
    public void whenGetProfile_thenSuccessResponse() throws Exception {

        createAndSaveClient();


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
        );

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
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test get profile with no content functionality")
    public void whenGetProfileNotFound_thenNoContentResponse() throws Exception {

        // given


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
        );

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
        createAndSaveClient();

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/me")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> "johndoe")
        );

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.authorities", notNullValue()));
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test delete profile functionality")
    public void whenDeleteProfile_thenNoContentResponse() throws Exception {

        // given

        Client client = (Client) createAndSaveClient().get("Client");


        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent())
        ;
        Client deletedClient = clientRepository.findById(1L)
                .orElseThrow(() -> new AssertionError("Client not found"));

        assertTrue(deletedClient.isDeleted(), "Client should be marked as deleted");
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test change password functionality")
    public void givenPasswordRequest_whenChangePassword_thenNoContentResponse() throws Exception {

        // given
        ChangePasswordRequest request = new ChangePasswordRequest(
                "password",
                "newPassword123"
        );

        Client client = (Client) createAndSaveClient().get("Client");

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
        );


        // then

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
        Client deletedClient = clientRepository.findById(1L)
                .orElseThrow(() -> new AssertionError("Client not found"));

        assertTrue(passwordEncoder.matches("newPassword123", deletedClient.getPassword()));
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test get document functionality")
    public void whenGetDocument_thenSuccessResponse() throws Exception {

        // given
        HashMap<String, Object> hashMap = createAndSaveClient();
        Client client = (Client) hashMap.get("Client");
        DocumentType dt = (DocumentType) hashMap.get("DocumentType");

        documentRepository.save(Document.builder()
                .client(client)
                .documentType(dt)
                .series("1234")
                .number("567890")
                .dateOfIssue(LocalDate.of(2020, 1, 15))
                .issuingAuthority("МВД России")
                .verified(true)
                .deleted(false)
                .build());

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentType").value("PASSPORT"))
                .andExpect(jsonPath("$.series").value("1234"))
                .andExpect(jsonPath("$.number").value("567890"))
                .andExpect(jsonPath("$.issuingAuthority").value("МВД России"))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test get document with no content functionality")
    public void whenGetDocumentNotFound_thenNoContentResponse() throws Exception {

        // given


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test create document functionality")
    public void givenDocumentDto_whenCreateDocument_thenSuccessResponse() throws Exception {

        // given
        HashMap<String, Object> hashMap = createAndSaveClient();
        DocumentType dt = (DocumentType) hashMap.get("DocumentType");



        documentRepository.deleteAll();

        CreateDocumentRequest request = CreateDocumentRequest.builder()
                .documentTypeId(dt.getId())
                .series("1234")
                .number("567890")
                .dateOfIssue(LocalDate.of(2020, 1, 15))
                .issuingAuthority("МВД России")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
        );

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentType").value("PASSPORT"))
                .andExpect(jsonPath("$.series").value("1234"))
                .andExpect(jsonPath("$.number").value("567890"))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test create document with invalid data functionality")
    public void givenInvalidDocumentDto_whenCreateDocument_thenValidationErrorResponse() throws Exception {

        // given
        createAndSaveClient();

        CreateDocumentRequest invalidRequest = CreateDocumentRequest.builder()
                .documentTypeId(null) // null значение
                .series(null) // null значение
                .number(null) // null значение
                .dateOfIssue(null) // null значение
                .issuingAuthority(null) // null значение
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(invalidRequest))
        );

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test update document functionality")
    public void givenUpdateDocumentDto_whenUpdateDocument_thenSuccessResponse() throws Exception {

        // given
        HashMap<String, Object> hashMap = createAndSaveClient();
        DocumentType dt = (DocumentType) hashMap.get("DocumentType");
        Client client = (Client) hashMap.get("Client");

        Document document = documentRepository.save(Document.builder()
                .client(client)
                .documentType(dt)
                .series("222222")
                .number("XYU")
                .dateOfIssue(LocalDate.of(2020, 1, 15))
                .issuingAuthority("МВД России")
                .verified(true)
                .deleted(false)
                .build());

        UpdateDocumentRequest request = UpdateDocumentRequest.builder()
                .documentTypeId(dt.getId())
                .series("5678")
                .number("123456")
                .dateOfIssue(LocalDate.of(2021, 3, 20))
                .issuingAuthority("МВД России")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
        );

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.series").value("5678"))
                .andExpect(jsonPath("$.number").value("123456"))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test update document with invalid data functionality")
    public void givenInvalidUpdateDocumentDto_whenUpdateDocument_thenValidationErrorResponse() throws Exception {

        // given
        createAndSaveClient();

        UpdateDocumentRequest invalidRequest = UpdateDocumentRequest.builder()
                .dateOfIssue(LocalDate.now().plusDays(1)) // дата в будущем
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(invalidRequest))
        );

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test update document with non-existent document functionality")
    public void givenUpdateDocumentDtoWithNonExistentDocument_whenUpdateDocument_thenErrorResponse() throws Exception {

        // given
        HashMap<String, Object> hashMap = createAndSaveClient();
        DocumentType dt = (DocumentType) hashMap.get("DocumentType");

        documentRepository.deleteAll();

        UpdateDocumentRequest request = UpdateDocumentRequest.builder()
                .documentTypeId(dt.getId())
                .series("5678")
                .number("123456")
                .dateOfIssue(LocalDate.of(2021, 3, 20))
                .issuingAuthority("МВД России")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
        );

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Document not found"));
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test delete document functionality")
    public void whenDeleteDocument_thenNoContentResponse() throws Exception {

        // given
        HashMap<String, Object> hashMap = createAndSaveClient();
        Client client = (Client) hashMap.get("Client");
        DocumentType dt = (DocumentType) hashMap.get("DocumentType");

        documentRepository.save(Document.builder()
                .client(client)
                .documentType(dt)
                .series("222222")
                .number("XYU")
                .dateOfIssue(LocalDate.of(2020, 1, 15))
                .issuingAuthority("МВД России")
                .verified(true)
                .deleted(false)
                .build());

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
        Document deletedDocument = documentRepository.findById(1L)
                .orElseThrow(() -> new AssertionError("Document not found"));

        assertTrue(deletedDocument.isDeleted(), "Document should be marked as deleted");

    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test delete document with non-existent document functionality")
    public void whenDeleteNonExistentDocument_thenErrorResponse() throws Exception {

        // given
        createAndSaveClient();

        documentRepository.deleteAll();

        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/document")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Document not found"));
    }

    @Test
    @WithMockClientDetails(username = "johndoe")
    @DisplayName("Test update profile functionality")
    public void givenUpdateProfileDto_whenUpdateProfile_thenNoContentResponse() throws Exception {

        // given
        createAndSaveClient();

        UpdateProfileRequest request = new UpdateProfileRequest(
                "Jane",
                "Smith",
                "+0987654321"
        );

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
        );

        // then

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        Client updatedClient = clientRepository.findById(1L)
                .orElseThrow(() -> new AssertionError("Client not found"));

        assertEquals(updatedClient.getFirstName(), "Jane");
        assertEquals(updatedClient.getLastName(), "Smith");
        assertEquals(updatedClient.getPhone(), "+0987654321");
    }

    @Test
    @WithMockClientDetails(username = "nonexistentuser", id = 999L)
    @DisplayName("Test update profile with non-existent user functionality")
    public void givenUpdateProfileDtoWithNonExistentUser_whenUpdateProfile_thenErrorResponse() throws Exception {

        // given
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Jane",
                "Smith",
                "+0987654321"
        );

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
        );

        // then

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Client not found"));
    }
}
