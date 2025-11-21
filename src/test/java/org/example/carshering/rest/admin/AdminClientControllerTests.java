package org.example.carshering.rest.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.request.RoleRequested;
import org.example.carshering.dto.response.AllUserResponse;
import org.example.carshering.dto.response.ShortUserResponse;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.exceptions.custom.RoleNotFoundException;
import org.example.carshering.rest.BaseWebMvcTest;
import org.example.carshering.service.interfaces.ClientService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminClientController.class
)
@Import(LocalValidatorFactoryBean.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminClientControllerTests extends BaseWebMvcTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/admin/users";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ClientService clientService;

    @Test
    @DisplayName("Test get user by id functionality")
    public void givenUserId_whenGetUser_thenSuccessResponse() throws Exception {

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

        given(clientService.findAllUser(1L)).willReturn(userResponse);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/1")
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
        given(clientService.findAllUser(999L)).willThrow(
                new NotFoundException("User not found")
        );

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/999")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("User not found"));
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
        ShortUserResponse user1 = new ShortUserResponse(1L, "user1", "user1@example.com", "CLIENT", false, false);
        ShortUserResponse user2 = new ShortUserResponse(2L, "user2", "user2@example.com", "ADMIN", false, false);

        Page<ShortUserResponse> page = new PageImpl<>(List.of(user1, user2));

        given(clientService.filterUsers(any(), any(Pageable.class))).willReturn(page);

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
        ShortUserResponse user = new ShortUserResponse(3L, "bannedUser", "banned@example.com", "CLIENT", true, false);
        Page<ShortUserResponse> page = new PageImpl<>(List.of(user));

        given(clientService.filterUsers(any(), any(Pageable.class))).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("banned", "true")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3L))
                .andExpect(jsonPath("$.content[0].login").value("bannedUser"))
                .andExpect(jsonPath("$.content[0].banned").value(true));
    }

    @Test
    @DisplayName("Test get all users with role filter functionality")
    public void givenRoleFilter_whenGetUsers_thenReturnFilteredPagedUsers() throws Exception {

        // given
        ShortUserResponse user = new ShortUserResponse(4L, "adminUser", "admin@example.com", "ADMIN", false, false);
        Page<ShortUserResponse> page = new PageImpl<>(List.of(user));

        given(clientService.filterUsers(any(), any(Pageable.class))).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("roleName", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(4L))
                .andExpect(jsonPath("$.content[0].roleName").value("ADMIN"));
    }

    @Test
    @DisplayName("Test get all users with multiple filters functionality")
    public void givenMultipleFilters_whenGetUsers_thenReturnFilteredPagedUsers() throws Exception {

        // given
        ShortUserResponse user = new ShortUserResponse(5L, "activeAdmin", "activeadmin@example.com", "ADMIN", false, false);
        Page<ShortUserResponse> page = new PageImpl<>(List.of(user));

        given(clientService.filterUsers(any(), any(Pageable.class))).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("banned", "false")
                .param("roleName", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5L))
                .andExpect(jsonPath("$.content[0].login").value("activeAdmin"))
                .andExpect(jsonPath("$.content[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$.content[0].banned").value(false));
    }

    @Test
    @DisplayName("Test get all users when empty result functionality")
    public void whenNoUsersFound_thenReturnEmptyPage() throws Exception {

        // given
        Page<ShortUserResponse> emptyPage = new PageImpl<>(List.of());

        given(clientService.filterUsers(any(), any(Pageable.class))).willReturn(emptyPage);

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
                .login("ivan123")
                .phone("+79991234567")
                .email("ivan@example.com")
                .roleName("CLIENT")
                .banned(true)
                .build();

        given(clientService.banUser(1L)).willReturn(bannedUser);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/ban")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("ivan123"))
                .andExpect(jsonPath("$.banned").value(true));
    }

    @Test
    @DisplayName("Test ban user with incorrect id functionality")
    public void givenIncorrectUserId_whenBanUser_thenErrorResponse() throws Exception {

        // given
        given(clientService.banUser(999L)).willThrow(
                new NotFoundException("User not found")
        );

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/999/ban")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Test unban user functionality")
    public void givenUserId_whenUnbanUser_thenSuccessResponse() throws Exception {

        // given
        AllUserResponse unbannedUser = AllUserResponse.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Иванов")
                .login("ivan123")
                .phone("+79991234567")
                .email("ivan@example.com")
                .roleName("CLIENT")
                .banned(false)
                .build();

        given(clientService.unbanUser(1L)).willReturn(unbannedUser);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/unban")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("ivan123"))
                .andExpect(jsonPath("$.banned").value(false));
    }

    @Test
    @DisplayName("Test unban user with incorrect id functionality")
    public void givenIncorrectUserId_whenUnbanUser_thenErrorResponse() throws Exception {

        // given
        given(clientService.unbanUser(999L)).willThrow(
                new NotFoundException("User not found")
        );

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/999/unban")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Test update user role functionality")
    public void givenRoleRequest_whenUpdateRole_thenSuccessResponse() throws Exception {

        // given
        RoleRequested roleRequest = new RoleRequested("ADMIN");
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

        given(clientService.updateRole(eq(1L), eq("ADMIN"))).willReturn(updatedUser);

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1/updateRole")
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

        given(clientService.updateRole(eq(999L), anyString())).willThrow(
                new NotFoundException("User not found")
        );

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
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Test update user role with non-existent role functionality")
    public void givenNonExistentRole_whenUpdateRole_thenErrorResponse() throws Exception {

        // given
        RoleRequested roleRequest = new RoleRequested("UNKNOWN_ROLE");

        given(clientService.updateRole(eq(1L), eq("UNKNOWN_ROLE"))).willThrow(
                new RoleNotFoundException("Role not found")
        );

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
                .andExpect(jsonPath("$.message").value("Role not found"));
    }

    @Test
    @DisplayName("Test ban already banned user functionality")
    public void givenAlreadyBannedUser_whenBanUser_thenSuccessResponse() throws Exception {

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

        given(clientService.banUser(1L)).willReturn(alreadyBannedUser);

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

        given(clientService.unbanUser(1L)).willReturn(alreadyActiveUser);

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

        given(clientService.filterUsers(any(), any(Pageable.class))).willReturn(page);

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

