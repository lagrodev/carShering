package org.example.carshering.it.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.fleet.infrastructure.persistence.repository.*;
import org.example.carshering.identity.api.dto.request.AuthRequest;
import org.example.carshering.identity.api.dto.request.RegistrationRequest;
import org.example.carshering.identity.infrastructure.persistence.entity.Client;
import org.example.carshering.identity.infrastructure.persistence.entity.Role;
import org.example.carshering.identity.infrastructure.persistence.repository.ClientRepository;
import org.example.carshering.identity.infrastructure.persistence.repository.DocumentRepository;
import org.example.carshering.identity.infrastructure.persistence.repository.DocumentTypeRepository;
import org.example.carshering.identity.infrastructure.persistence.repository.RoleRepository;
import org.example.carshering.it.BaseWebIntegrateTest;
import org.example.carshering.rental.infrastructure.persistence.repository.ContractRepository;
import org.example.carshering.repository.RentalStateRepository;
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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIntegrationTests extends BaseWebIntegrateTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api";

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


    @Test
    @DisplayName("Test create auth token functionality")
    public void givenJwtRequest_whenCreateAuthToken_thenSuccessResponse() throws Exception {

        // given
        Role role = roleRepository.save(Role.builder().name("CLIENT").build());

        Client client = Client.builder()
                .firstName("Иван")
                .lastName("Иванов")
                .login("user101")
                .phone("+79991234567")
                .email("user101@example.com")
                .role(role)
                .password("$2a$10$9Q034ZxTRJaajMYVg5YRRehsaGV.VdcaeUFkRrJDNwhBAIOZefKJa") // хешированный "password"
                .build();
        clientRepository.save(client);

        AuthRequest authRequest = new AuthRequest("user101", "password");

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(authRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("access_token")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Path=/api")))
                .andExpect(jsonPath("$.message").value("Authentication successful"));
    }


    @Test
    @DisplayName("Test create auth token with null credentials functionality")
    public void givenNullCredentials_whenCreateAuthToken_thenBadRequestResponse() throws Exception {

        // given
        AuthRequest authRequest = new AuthRequest(null, null);

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(authRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()); // изменено на корректный статус
    }


    @Test
    @DisplayName("Test create auth token with empty request body functionality")
    public void givenEmptyBody_whenCreateAuthToken_thenBadRequestResponse() throws Exception {

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()); // изменено на корректный статус
    }


    @Test
    @DisplayName("Test registration functionality")
    public void givenRegistrationRequest_whenCreateNewUser_thenSuccessResponse() throws Exception {

        // given
        roleRepository.save(Role.builder().name("CLIENT").build());

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .login("newuser123")
                .password("password123")
                .lastName("Ivanov")
                .email("ivanov@example.com")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registrationRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.lastName").value("Ivanov"))
                .andExpect(jsonPath("$.login").value("newuser123"))
                .andExpect(jsonPath("$.email").value("ivanov@example.com"));
    }


    @Test
    @DisplayName("Test registration with invalid data functionality")
    public void givenInvalidRegistrationRequest_whenCreateNewUser_thenValidationErrorResponse() throws Exception {

        // given - короткий логин и пароль, пустая фамилия, невалидный email
        RegistrationRequest invalidRequest = RegistrationRequest.builder()
                .login("usr") // меньше 6 символов
                .password("123") // меньше 6 символов
                .lastName("") // пустая фамилия
                .email("invalid-email") // невалидный email
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/registration")
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
    @DisplayName("Test registration with blank fields functionality")
    public void givenBlankFields_whenCreateNewUser_thenValidationErrorResponse() throws Exception {

        // given - все поля пустые
        RegistrationRequest invalidRequest = RegistrationRequest.builder()
                .login("")
                .password("")
                .lastName("")
                .email("")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/registration")
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
    @DisplayName("Test registration with duplicate login functionality")
    public void givenDuplicateLogin_whenCreateNewUser_thenErrorResponse() throws Exception {

        // given
        Role role = roleRepository.save(Role.builder().name("CLIENT").build());

        Client existingClient = dataUtils.createAndSaveClient(
                "Петр",
                "Петров",
                "existinguser",
                "+79991234567",
                "existing@example.com",
                role,
                "$2a$10$9Q034ZxTRJaajMYVg5YRRehsaGV.VdcaeUFkRrJDNwhBAIOZefKJa"
        );
        clientRepository.save(existingClient);

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .login("existinguser")
                .password("$2a$10$9Q034ZxTRJaajMYVg5YRRehsaGV.VdcaeUFkRrJDNwhBAIOZefKJa")
                .lastName("Petrov")
                .email("petrov@example.com")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registrationRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("AlreadyExistsException")))
                .andExpect(jsonPath("$.message").value("Login already in use"));
    }


    @Test
    @DisplayName("Test registration with too long login functionality")
    public void givenTooLongLogin_whenCreateNewUser_thenValidationErrorResponse() throws Exception {

        // given - логин больше 50 символов
        String longLogin = "a".repeat(51);
        RegistrationRequest invalidRequest = RegistrationRequest.builder()
                .login(longLogin)
                .password("password123")
                .lastName("Sidorov")
                .email("sidorov@example.com")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/registration")
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
    @DisplayName("Test logout functionality")
    public void givenValidRequest_whenLogout_thenSuccessResponse() throws Exception {

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/logout")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("access_token=")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
                .andExpect(jsonPath("$.message").value("Logged out"));
    }


    @Test
    @DisplayName("Test registration with null fields functionality")
    public void givenNullFields_whenCreateNewUser_thenValidationErrorResponse() throws Exception {

        // given - null поля
        RegistrationRequest invalidRequest = RegistrationRequest.builder()
                .login(null)
                .password(null)
                .lastName(null)
                .email(null)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/registration")
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
    @DisplayName("Test registration with valid minimal data functionality")
    public void givenValidMinimalData_whenCreateNewUser_thenSuccessResponse() throws Exception {

        // given - минимально валидные данные (по границе валидации)
        roleRepository.save(Role.builder().name("CLIENT").build());

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .login("user12") // ровно 6 символов
                .password("pass12") // ровно 6 символов
                .lastName("Li")
                .email("a@b.ru") // минимальный валидный email
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registrationRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.lastName").value("Li"))
                .andExpect(jsonPath("$.login").value("user12"))
                .andExpect(jsonPath("$.email").value("a@b.ru"));
    }

}
