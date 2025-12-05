package org.example.carshering.it.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.request.RoleRequested;
import org.example.carshering.dto.response.AllUserResponse;
import org.example.carshering.dto.response.ShortUserResponse;
import org.example.carshering.domain.entity.Client;
import org.example.carshering.domain.entity.Role;
import org.example.carshering.it.BaseWebIntegrateTest;
import org.example.carshering.repository.*;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminClientControllerIntegrationTests extends BaseWebIntegrateTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/admin/users";


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


    @BeforeEach
    void resetSequences() {
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.contract_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.doctype_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.document_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.client_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.role_id_seq RESTART WITH 1");
    }

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        documentRepository.deleteAll();
        documentTypeRepository.deleteAll();
        contractRepository.deleteAll();
        clientRepository.deleteAll();
        roleRepository.deleteAll();
    }


    @Test
    @DisplayName("Test get user by id functionality")
    public void givenUserId_whenGetUser_thenSuccessResponse() throws Exception {

        Role role = roleRepository.save(Role.builder().name("CLIENT").build());

        Client client = dataUtils.createAndSaveClient("Иван", "Иванов", "ivan123",
                "+79991234567", "ivan@example.com", role, "password");
        clientRepository.save(client);


        // given
        AllUserResponse userResponse = AllUserResponse.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .login("ivan123")
                .phone("+79991234567")
                .email("ivan@example.com")
                .roleName("CLIENT")
                .banned(false)
                .build();


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/" + client.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Иванов"))
                .andExpect(jsonPath("$.login").value("ivan123"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.roleName").value("CLIENT"))
                .andExpect(jsonPath("$.banned").value(false));
    }

    @Test
    @DisplayName("Test get user by incorrect id functionality")
    public void givenIncorrectUserId_whenGetUser_thenErrorResponse() throws Exception {

        // given


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/999")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Client not found"));
    }

    @Test
    @DisplayName("Test get user with non-numeric userId functionality")
    public void givenNonNumericUserId_whenGetUser_thenBadRequestResponse() throws Exception {

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/invalid-id")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'userId': 'invalid-id'")));
    }

    @Test
    @DisplayName("Test get all users without filters functionality")
    public void whenGetAllUsersWithoutFilters_thenReturnPagedUsers() throws Exception {

        // given
        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());


        ShortUserResponse user1 = new ShortUserResponse(1L, "user1", "user1@example.com", "CLIENT", false, false);
        ShortUserResponse user2 = new ShortUserResponse(2L, "user2", "user2@example.com", "ADMIN", false, false);

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "user1",
                "+79991234567", "user1@example.com", role1, "password");

        Client client2 = dataUtils.createAndSaveClient("Иван", "Иванов", "user2",
                "+79939123467", "user2@example.com", role2, "password");
        clientRepository.save(client1);
        clientRepository.save(client2);

        System.out.println("Хуйня какая-то");
        System.out.println(clientRepository.findAll());


        Page<ShortUserResponse> page = new PageImpl<>(List.of(user1, user2));


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].login").value("user1"))
                .andExpect(jsonPath("$.content[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$.content[0].roleName").value("CLIENT"))
                .andExpect(jsonPath("$.content[0].banned").value(false))
                .andExpect(jsonPath("$.content[1].login").value("user2"))
                .andExpect(jsonPath("$.content[1].roleName").value("ADMIN"));
    }

    @Test
    @DisplayName("Test get all users with banned filter functionality")
    public void givenBannedFilter_whenGetUsers_thenReturnFilteredPagedUsers() throws Exception {

        // given
        ShortUserResponse user1 = new ShortUserResponse(1L, "user1", "user1@example.com", "CLIENT", true, false);
        ShortUserResponse user2 = new ShortUserResponse(2L, "user2", "user2@example.com", "ADMIN", false, false);
        Page<ShortUserResponse> page = new PageImpl<>(List.of(user1, user2));
        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "user1",
                "+79991234567", "user1@example.com", role1, "password");

        Client client2 = dataUtils.createAndSaveClient("Иван", "Иванов", "user2",
                "+79939123467", "user2@example.com", role2, "password");
        client1.setBanned(true); // ибо чотко
        clientRepository.save(client1);

        clientRepository.save(client2);
        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("banned", "true")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].login").value("user1"))
                .andExpect(jsonPath("$.content[0].banned").value(true));
    }

    @Test
    @DisplayName("Test get all users with role filter functionality")
    public void givenRoleFilter_whenGetUsers_thenReturnFilteredPagedUsers() throws Exception {

        // given

        ShortUserResponse user1 = new ShortUserResponse(1L, "user1", "user1@example.com", "CLIENT", true, false);
        ShortUserResponse user2 = new ShortUserResponse(2L, "user2", "user2@example.com", "ADMIN", false, false);
        Page<ShortUserResponse> page = new PageImpl<>(List.of(user1, user2));
        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "user1",
                "+79991234567", "user1@example.com", role1, "password");

        Client client2 = dataUtils.createAndSaveClient("Иван", "Иванов", "user2",
                "+79939123467", "user2@example.com", role2, "password");

        clientRepository.save(client1);

        clientRepository.save(client2);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("roleName", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2L))
                .andExpect(jsonPath("$.content[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$.content[0].login").value("user2"))
                .andExpect(jsonPath("$.content[0].email").value("user2@example.com"))

        ;
    }

    @Test
    @DisplayName("Test get all users with multiple filters functionality")
    public void givenMultipleFilters_whenGetUsers_thenReturnFilteredPagedUsers() throws Exception {

        // given
        ShortUserResponse user1 = new ShortUserResponse(1L, "user1", "user1@example.com", "CLIENT", true, false);
        ShortUserResponse user2 = new ShortUserResponse(2L, "user2", "user2@example.com", "ADMIN", false, false);
        ShortUserResponse user3 = new ShortUserResponse(2L, "user3", "user3@example.com", "ADMIN", true, false);
        Page<ShortUserResponse> page = new PageImpl<>(List.of(user1, user2));
        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "user1",
                "+79991234567", "user1@example.com", role1, "password");

        Client client2 = dataUtils.createAndSaveClient("Иван", "Иванов", "user2",
                "+79939123467", "user2@example.com", role2, "password");
        Client client3 = dataUtils.createAndSaveClient("Иван", "Иванов", "user3",
                "+7993912311467", "user3@example.com", role2, "password");

        clientRepository.save(client1);

        clientRepository.save(client2);

        client3.setBanned(true);
        client1.setBanned(true);

        clientRepository.save(client3);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("banned", "true")
                .param("roleName", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3L))
                .andExpect(jsonPath("$.content[0].login").value("user3"))
                .andExpect(jsonPath("$.content[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$.content[0].email").value("user3@example.com"))
                .andExpect(jsonPath("$.content[0].banned").value(true))
                .andExpect(jsonPath("$.page.totalElements").value(1))

        ;
    }

    @Test
    @DisplayName("Test get all users when empty result functionality")
    public void whenNoUsersFound_thenReturnEmptyPage() throws Exception {

        // given
        Page<ShortUserResponse> emptyPage = new PageImpl<>(List.of());


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("roleName", "UNKNOWN_ROLE")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @DisplayName("Test ban user functionality")
    public void givenUserId_whenBanUser_thenSuccessResponse() throws Exception {

        // given
        AllUserResponse bannedUser = AllUserResponse.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .login("user1")
                .phone("+79991234567")
                .email("user1@example.com")
                .roleName("CLIENT")
                .banned(true)
                .build();

        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "user1",
                "+79991234567", "user1@example.com", role1, "password");

        clientRepository.save(client1);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/" + client1.getId() + "/ban")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("user1"))
                .andExpect(jsonPath("$.banned").value(true));
    }

    @Test
    @DisplayName("Test ban user with incorrect id functionality")
    public void givenIncorrectUserId_whenBanUser_thenErrorResponse() throws Exception {

        // given


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/999/ban")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Client not found"));
    }

    @Test
    @DisplayName("Test unban user functionality")
    public void givenUserId_whenUnbanUser_thenSuccessResponse() throws Exception {

        // given
        AllUserResponse unbannedUser = AllUserResponse.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .login("user1")
                .phone("+79991234567")
                .email("user1@example.com")
                .roleName("CLIENT")
                .banned(true)
                .build();

        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "user1",
                "+79991234567", "user1@example.com", role1, "password");

        clientRepository.save(client1);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/unban")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("user1"))
                .andExpect(jsonPath("$.banned").value(false));
    }

    @Test
    @DisplayName("Test unban user with incorrect id functionality")
    public void givenIncorrectUserId_whenUnbanUser_thenErrorResponse() throws Exception {

        // given


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/999/unban")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Client not found"));
    }

    @Test
    @DisplayName("Test update user role functionality")
    public void givenRoleRequest_whenUpdateRole_thenSuccessResponse() throws Exception {

        // given
        RoleRequested roleRequest = new RoleRequested("ADMIN");

        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "ivan123",
                "+79991234567", "ivan@example.com", role1, "password");

        clientRepository.save(client1);

        AllUserResponse updatedUser = AllUserResponse.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .login("ivan123")
                .phone("+79991234567")
                .email("ivan@example.com")
                .roleName("ADMIN")
                .banned(false)
                .build();


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/" + client1.getId() + "/updateRole")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(roleRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("ivan123"))
                .andExpect(jsonPath("$.roleName").value("ADMIN"));
    }

    @Test
    @DisplayName("Test update user role with invalid role name functionality")
    public void givenInvalidRoleRequest_whenUpdateRole_thenValidationErrorResponse() throws Exception {

        // given
        RoleRequested invalidRequest = new RoleRequested(""); // пустое имя роли
        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "ivan123",
                "+79991234567", "ivan@example.com", role1, "password");

        clientRepository.save(client1);
        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/updateRole")
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
    @DisplayName("Test update user role with incorrect user id functionality")
    public void givenIncorrectUserId_whenUpdateRole_thenErrorResponse() throws Exception {

        // given
        RoleRequested roleRequest = new RoleRequested("ADMIN");

        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/999/updateRole")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(roleRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Client not found with id 999"));
    }

    @Test
    @DisplayName("Test update user role with non-existent role functionality")
    public void givenNonExistentRole_whenUpdateRole_thenErrorResponse() throws Exception {

        // given
        RoleRequested roleRequest = new RoleRequested("UNKNOWN_ROLE");

        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "ivan123",
                "+79991234567", "ivan@example.com", role1, "password");

        clientRepository.save(client1);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/updateRole")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(roleRequest)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("RoleNotFoundException")))
                .andExpect(jsonPath("$.message").value("Role not found with name: UNKNOWN_ROLE"));
    }

    @Test
    @DisplayName("Test ban already banned user functionality")
    public void givenAlreadyBannedUser_whenBanUser_thenSuccessResponse() throws Exception {
        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "ivan123",
                "+79991234567", "ivan@example.com", role1, "password");
        client1.setBanned(true);

        clientRepository.save(client1);
        // given
        AllUserResponse alreadyBannedUser = AllUserResponse.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .login("ivan123")
                .phone("+79991234567")
                .email("ivan@example.com")
                .roleName("CLIENT")
                .banned(true) // уже заблокирован
                .build();


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/ban")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.banned").value(true));
    }

    @Test
    @DisplayName("Test unban already active user functionality")
    public void givenAlreadyActiveUser_whenUnbanUser_thenSuccessResponse() throws Exception {
        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "ivan123",
                "+79991234567", "ivan@example.com", role1, "password");

        clientRepository.save(client1);
        // given
        AllUserResponse alreadyActiveUser = AllUserResponse.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .login("ivan123")
                .phone("+79991234567")
                .email("ivan@example.com")
                .roleName("CLIENT")
                .banned(false) // уже активен
                .build();


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/unban")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.banned").value(false));
    }

    @Test
    @DisplayName("Test get users with pagination functionality")
    public void givenPaginationParams_whenGetUsers_thenReturnCorrectPage() throws Exception {

        // given
        ShortUserResponse user1 = new ShortUserResponse(1L, "user1", "user1@example.com", "CLIENT", false, false);
        ShortUserResponse user2 = new ShortUserResponse(2L, "user2", "user2@example.com", "CLIENT", false, false);

        Page<ShortUserResponse> page = new PageImpl<>(List.of(user1, user2));

        Role role1 = roleRepository.save(Role.builder().name("CLIENT").build());
        Role role2 = roleRepository.save(Role.builder().name("ADMIN").build());

        Client client1 = dataUtils.createAndSaveClient("Иван", "Иванов", "user1",
                "+79991234567", "user1@example.com", role1, "password");

        Client client2 = dataUtils.createAndSaveClient("Иван", "Иванов", "user2",
                "+79939123467", "user2@example.com", role2, "password");

        clientRepository.save(client1);

        clientRepository.save(client2);
        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("page", "0")
                .param("size", "2")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)));
    }

}

