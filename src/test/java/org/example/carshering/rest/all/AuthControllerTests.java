package org.example.carshering.rest.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.request.AuthRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.exceptions.custom.AlreadyExistsException;
import org.example.carshering.rest.BaseWebMvcTest;
import org.example.carshering.service.interfaces.AuthService;
import org.example.carshering.service.interfaces.ClientService;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class
)
@Import(LocalValidatorFactoryBean.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTests extends BaseWebMvcTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ClientService clientService;


    @Test
    @DisplayName("Test create auth token functionality")
    public void givenJwtRequest_whenCreateAuthToken_thenSuccessResponse() throws Exception {

        // given
        AuthRequest authRequest = new AuthRequest("user101", "password");
        String token = "mock.jwt.token";

        given(authService.createAuthToken(any(AuthRequest.class)))
                .willReturn(ResponseCookie.from(token, "mock.jwt.token").build());

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
                .andExpect(status().isBadRequest()); // контроллер не валидирует, поэтому ок
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
                .andExpect(status().isBadRequest())

        ; // контроллер примет пустой запрос
    }


    @Test
    @DisplayName("Test registration functionality")
    public void givenRegistrationRequest_whenCreateNewUser_thenSuccessResponse() throws Exception {

        // given
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .login("newuser123")
                .password("password123")
                .lastName("Ivanov")
                .email("ivanov@example.com")
                .build();

        UserResponse userResponse = new UserResponse(
                1L,
                null,
                "Ivanov",
                "newuser123",
                null,
                "ivanov@example.com", false
        );

        given(clientService.createUser(any(RegistrationRequest.class))).willReturn(userResponse);

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
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .login("existinguser")
                .password("password123")
                .lastName("Petrov")
                .email("petrov@example.com")
                .build();

        given(clientService.createUser(any(RegistrationRequest.class)))
                .willThrow(new AlreadyExistsException("User with this login already exists"));

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
                .andExpect(jsonPath("$.message").value("User with this login already exists"));
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
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .login("user12") // ровно 6 символов
                .password("pass12") // ровно 6 символов
                .lastName("Li")
                .email("a@b.ru") // минимальный валидный email
                .build();

        UserResponse userResponse = new UserResponse(
                2L,
                null,
                "Li",
                "user12",
                null,
                "a@b.ru", false
        );

        given(clientService.createUser(any(RegistrationRequest.class))).willReturn(userResponse);

        // when
        ResultActions resultActions = mockMvc.perform(post(apiUrl + "/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registrationRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.lastName").value("Li"))
                .andExpect(jsonPath("$.login").value("user12"))
                .andExpect(jsonPath("$.email").value("a@b.ru"));
    }

}

