package org.example.carshering.it.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Document;
import org.example.carshering.entity.DocumentType;
import org.example.carshering.entity.Role;
import org.example.carshering.it.BaseWebIntegrateTest;
import org.example.carshering.repository.ClientRepository;
import org.example.carshering.repository.DocumentRepository;
import org.example.carshering.repository.DocumentTypeRepository;
import org.example.carshering.repository.RoleRepository;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminDocumentControllerIntegrationTests extends BaseWebIntegrateTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/admin/documents";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void resetSequences() {
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.document_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.doctype_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.client_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.role_id_seq RESTART WITH 1");
    }

    @BeforeEach
    @Transactional
    public void setup() {
        documentRepository.deleteAll();
        documentTypeRepository.deleteAll();
        clientRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName("Test get all documents with only unverified functionality")
    public void givenOnlyUnverifiedTrue_whenGetAllDocuments_thenSuccessResponse() throws Exception {

        // given
        Role role = roleRepository.save(Role.builder().name("CLIENT").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "ivan123",
                "+79991234567", "ivan@example.com", role, "password");
        Client client2 = dataUtils.createAndSaveClient("Петр", "Петров", "petr123",
                "+79991234568", "petr@example.com", role, "password");
        clientRepository.save(client1);
        clientRepository.save(client2);

        DocumentType docType1 = documentTypeRepository.save(DataUtils.getDocumentTypePersisted("Паспорт"));
        DocumentType docType2 = documentTypeRepository.save(DataUtils.getDocumentTypePersisted("Водительское удостоверение"));

        Document doc1 = dataUtils.createDocumentTransient(client1, docType1, "1234", "567890", "УФМС", false);
        Document doc2 = dataUtils.createDocumentTransient(client2, docType2, "5678", "123456", "ГИБДД", false);
        documentRepository.save(doc1);
        documentRepository.save(doc2);

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
                .andExpect(jsonPath("$.content[1].verified").value(false));}

    @Test
    @DisplayName("Test get all documents with only unverified false functionality")
    public void givenOnlyUnverifiedFalse_whenGetAllDocuments_thenSuccessResponse() throws Exception {

        // given
        Role role = roleRepository.save(Role.builder().name("CLIENT").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "ivan123",
                "+79991234567", "ivan@example.com", role, "password");
        Client client2 = dataUtils.createAndSaveClient("Петр", "Петров", "petr123",
                "+79991234568", "petr@example.com", role, "password");
        clientRepository.save(client1);
        clientRepository.save(client2);

        DocumentType docType = documentTypeRepository.save(DataUtils.getDocumentTypePersisted("Паспорт"));

        Document doc1 = dataUtils.createDocumentTransient(client1, docType, "1234", "567890", "УФМС", true);
        Document doc2 = dataUtils.createDocumentTransient(client2, docType, "5678", "123456", "УФМС", false);
        documentRepository.save(doc1);
        documentRepository.save(doc2);

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
        Role role = roleRepository.save(Role.builder().name("CLIENT").build());

        Client client = dataUtils.createAndSaveClient("Иван", "Иванов", "ivan123",
                "+79991234567", "ivan@example.com", role, "password");
        clientRepository.save(client);

        DocumentType docType = documentTypeRepository.save(DataUtils.getDocumentTypePersisted("Паспорт"));

        Document doc1 = dataUtils.createDocumentTransient(client, docType, "1234", "567890", "УФМС", false);
        documentRepository.save(doc1);

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
        Role role = roleRepository.save(Role.builder().name("CLIENT").build());

        Client client = dataUtils.createAndSaveClient("Иван", "Иванов", "ivan123",
                "+79991234567", "ivan@example.com", role, "password");
        clientRepository.save(client);

        DocumentType docType = documentTypeRepository.save(DataUtils.getDocumentTypePersisted("Паспорт"));

        Document document = dataUtils.createDocumentTransient(client, docType, "1234", "567890", "УФМС", false);
        documentRepository.save(document);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/{documentId}/verify", document.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test verify document with incorrect id functionality")
    public void givenIncorrectDocumentId_whenVerifyDocument_thenErrorResponse() throws Exception {

        // given


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/999/verify")
                .contentType(MediaType.APPLICATION_JSON));

        // then

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
