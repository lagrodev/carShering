package org.example.carshering.service.impl;

import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.dto.request.FilterUserRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.request.update.UpdateProfileRequest;
import org.example.carshering.dto.response.AllUserResponse;
import org.example.carshering.dto.response.ShortUserResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.domain.entity.Client;
import org.example.carshering.domain.entity.Role;
import org.example.carshering.exceptions.custom.*;
import org.example.carshering.mapper.ClientMapper;
import org.example.carshering.repository.ClientRepository;
import org.example.carshering.service.interfaces.EmailService;
import org.example.carshering.service.domain.ContractServiceHelper;
import org.example.carshering.service.domain.RoleServiceHelper;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTests {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private EmailService emailService;


    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleServiceHelper roleService;

    @Mock
    private ContractServiceHelper contractService;

    @InjectMocks
    private ClientServiceImpl serviceUnderTest;

    private final DataUtils dataUtils = new DataUtils();

    @Test
    @DisplayName("Test create user functionality")
    public void givenUserToCreate_whenCreateUser_thenRepositoryIsCalled() {
        // given
        RegistrationRequest request = RegistrationRequest.builder()
                .login("testuser")
                .password("password123")
                .lastName("TestLast")
                .email("test@example.com")
                .build();

        Client clientEntity = dataUtils.createAndSaveClient("testuser", "test@example.com");
        Role role = dataUtils.getRolePersisted();
        clientEntity.setRole(role);

        Client savedClient = dataUtils.createAndSaveClient("testuser", "test@example.com");
        savedClient.setId(1L);
        savedClient.setRole(role);

        UserResponse response = new UserResponse(1L, "First", "Last", "testuser", "123456", "test@example.com", false);

        given(clientRepository.existsByLoginAndDeletedFalse(request.login())).willReturn(false);
        given(clientRepository.existsByEmailAndDeletedFalse(request.email())).willReturn(false);
        given(clientMapper.toEntity(request)).willReturn(clientEntity);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(roleService.getRoleByName("CLIENT")).willReturn(role);
        given(clientRepository.save(clientEntity)).willReturn(savedClient);
        given(clientMapper.toDto(savedClient)).willReturn(response);

        // when
        UserResponse actual = serviceUnderTest.createUser(request);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.login()).isEqualTo("testuser");
        assertThat(actual.email()).isEqualTo("test@example.com");

        verify(clientRepository).existsByLoginAndDeletedFalse(request.login());
        verify(clientRepository).existsByEmailAndDeletedFalse(request.email());
        verify(clientMapper).toEntity(request);
        verify(passwordEncoder).encode(anyString());
        verify(roleService).getRoleByName("CLIENT");
        verify(clientRepository).save(clientEntity);
        verify(clientMapper).toDto(savedClient);
    }


    @Test
    @DisplayName("Test create user assigns CLIENT role correctly")
    public void givenNewUser_whenCreateUser_thenClientRoleIsAssigned() {
        // given
        RegistrationRequest request = RegistrationRequest.builder()
                .login("rolecheck")
                .password("password123")
                .lastName("Last")
                .email("role@example.com")
                .build();

        Client clientEntity = dataUtils.createAndSaveClient("rolecheck", "role@example.com");
        Role role = dataUtils.getRolePersisted();
        clientEntity.setRole(null);

        Client savedClient = dataUtils.createAndSaveClient("rolecheck", "role@example.com");
        savedClient.setId(5L);
        savedClient.setRole(role);

        UserResponse response = new UserResponse(5L, "F", "L", "rolecheck", "000", "role@example.com", false);

        given(clientRepository.existsByLoginAndDeletedFalse(request.login())).willReturn(false);
        given(clientRepository.existsByEmailAndDeletedFalse(request.email())).willReturn(false);
        given(clientMapper.toEntity(request)).willReturn(clientEntity);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(roleService.getRoleByName("CLIENT")).willReturn(role);
        given(clientRepository.save(clientEntity)).willReturn(savedClient);
        given(clientMapper.toDto(savedClient)).willReturn(response);

        // when
        UserResponse actual = serviceUnderTest.createUser(request);

        // then
        assertThat(actual).isNotNull();
        assertThat(savedClient.getRole()).isEqualTo(role);

        verify(roleService).getRoleByName("CLIENT");
        verify(clientRepository).save(clientEntity);
    }


    @Test
    @DisplayName("Test create user encodes password before saving")
    public void givenUserToCreate_whenCreateUser_thenPasswordIsEncodedBeforeSave() {
        // given
        RegistrationRequest request = RegistrationRequest.builder()
                .login("encodeduser")
                .password("plainPassword")
                .lastName("TestLast")
                .email("encode@example.com")
                .build();

        Client clientEntity = dataUtils.createAndSaveClient("encodeduser", "encode@example.com");
        Role role = dataUtils.getRolePersisted();
        clientEntity.setRole(role);
        clientEntity.setPassword("plainPassword");

        Client savedClient = dataUtils.createAndSaveClient("encodeduser", "encode@example.com");
        savedClient.setId(2L);
        savedClient.setPassword("encodedPassword");
        savedClient.setRole(role);

        UserResponse response = new UserResponse(2L, "First", "Last", "encodeduser", "123456", "encode@example.com", false);

        given(clientRepository.existsByLoginAndDeletedFalse(request.login())).willReturn(false);
        given(clientRepository.existsByEmailAndDeletedFalse(request.email())).willReturn(false);
        given(clientMapper.toEntity(request)).willReturn(clientEntity);
        given(passwordEncoder.encode("plainPassword")).willReturn("encodedPassword");
        given(roleService.getRoleByName("CLIENT")).willReturn(role);
        given(clientRepository.save(clientEntity)).willReturn(savedClient);
        given(clientMapper.toDto(savedClient)).willReturn(response);

        // when
        UserResponse actual = serviceUnderTest.createUser(request);

        // then
        assertThat(actual).isNotNull();
        assertThat(clientEntity.getPassword()).isEqualTo("encodedPassword");

        verify(passwordEncoder).encode("plainPassword");
        verify(clientRepository).save(clientEntity);
    }


    @Test
    @DisplayName("Test create user with duplicate login throws exception")
    public void givenUserWithDuplicateLogin_whenCreateUser_thenThrowException() {
        // given
        RegistrationRequest request = RegistrationRequest.builder()
                .login("existinguser")
                .password("password123")
                .lastName("TestLast")
                .email("test@example.com")
                .build();

        given(clientRepository.existsByLoginAndDeletedFalse(request.login())).willReturn(true);

        // when + then
        assertThrows(
                AlreadyExistsException.class,
                () -> serviceUnderTest.createUser(request)
        );

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Test create user with duplicate email throws exception")
    public void givenUserWithDuplicateEmail_whenCreateUser_thenThrowException() {
        // given
        RegistrationRequest request = RegistrationRequest.builder()
                .login("newuser")
                .password("password123")
                .lastName("TestLast")
                .email("existing@example.com")
                .build();

        given(clientRepository.existsByLoginAndDeletedFalse(request.login())).willReturn(false);
        given(clientRepository.existsByEmailAndDeletedFalse(request.email())).willReturn(true);

        // when + then
        assertThrows(
                AlreadyExistsException.class,
                () -> serviceUnderTest.createUser(request)
        );

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Test find user by id functionality")
    public void givenUserId_whenFindUser_thenUserIsReturned() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);

        UserResponse response = new UserResponse(1L, "First", "Last", "testuser", "123456", "test@example.com", false);

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(clientMapper.toDto(client)).willReturn(response);

        // when
        UserResponse actual = serviceUnderTest.findUser(1L);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(1L);
        assertThat(actual.login()).isEqualTo("testuser");

        verify(clientRepository).findById(1L);
        verify(clientMapper).toDto(client);
    }

    @Test
    @DisplayName("Test find user by id with non-existing user returns null")
    public void givenNonExistingUserId_whenFindUser_thenNullIsReturned() {
        // given
        given(clientRepository.findById(1L)).willReturn(Optional.empty());
        given(clientMapper.toDto(null)).willReturn(null);

        // when
        UserResponse actual = serviceUnderTest.findUser(1L);

        // then
        assertThat(actual).isNull();

        verify(clientRepository).findById(1L);
        verify(clientMapper).toDto(null);
    }

    @Test
    @DisplayName("Test find all user functionality")
    public void givenUserId_whenFindAllUser_thenAllUserResponseIsReturned() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        Role role = dataUtils.getRolePersisted();
        client.setRole(role);

        AllUserResponse response = AllUserResponse.builder()
                .id(1L)
                .login("testuser")
                .email("test@example.com")
                .roleName("PersistedRole")
                .banned(false)
                .build();

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(clientMapper.toDtoForAdmin(client)).willReturn(response);

        // when
        AllUserResponse actual = serviceUnderTest.findAllUser(1L);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(1L);
        assertThat(actual.login()).isEqualTo("testuser");
        assertThat(actual.banned()).isFalse();

        verify(clientRepository).findById(1L);
        verify(clientMapper).toDtoForAdmin(client);
    }


    @Test
    @DisplayName("Test find all user throws exception when user not found")
    public void givenNonExistingUserId_whenFindAllUser_thenThrowException() {
        // given
        given(clientRepository.findById(10L)).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.findAllUser(10L)
        );

        verify(clientRepository).findById(10L);
        verify(clientMapper, never()).toDtoForAdmin(any());
    }

    @Test
    @DisplayName("Test ban user functionality")
    public void givenUserId_whenBanUser_thenUserIsBanned() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        client.setBanned(false);
        Role role = dataUtils.getRolePersisted();
        client.setRole(role);

        AllUserResponse response = AllUserResponse.builder()
                .id(1L)
                .login("testuser")
                .email("test@example.com")
                .roleName("PersistedRole")
                .banned(true)
                .build();

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(clientRepository.save(client)).willReturn(client);
        given(clientMapper.toDtoForAdmin(client)).willReturn(response);

        // when
        AllUserResponse actual = serviceUnderTest.banUser(1L);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.banned()).isTrue();

        verify(clientRepository).findById(1L);
        verify(clientRepository).save(client);
        verify(clientMapper).toDtoForAdmin(client);
    }

    @Test
    @DisplayName("Test unban user functionality")
    public void givenUserId_whenUnbanUser_thenUserIsUnbanned() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        client.setBanned(true);
        Role role = dataUtils.getRolePersisted();
        client.setRole(role);

        AllUserResponse response = AllUserResponse.builder()
                .id(1L)
                .login("testuser")
                .email("test@example.com")
                .roleName("PersistedRole")
                .banned(false)
                .build();

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(clientRepository.save(client)).willReturn(client);
        given(clientMapper.toDtoForAdmin(client)).willReturn(response);

        // when
        AllUserResponse actual = serviceUnderTest.unbanUser(1L);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.banned()).isFalse();

        verify(clientRepository).findById(1L);
        verify(clientRepository).save(client);
        verify(clientMapper).toDtoForAdmin(client);
    }

    @Test
    @DisplayName("Test delete user functionality")
    public void givenUserId_whenDeleteUser_thenUserIsDeleted() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        client.setDeleted(false);

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        doNothing().when(contractService).checkAndAllActiveContractsByClient(client);
        given(clientRepository.save(client)).willReturn(client);

        // when
        serviceUnderTest.deleteUser(1L);

        // then
        verify(clientRepository).findById(1L);
        verify(contractService).checkAndAllActiveContractsByClient(client);
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("Test delete user wraps BusinessConflictException with custom message")
    public void givenActiveContracts_whenDeleteUser_thenThrowsWrappedBusinessConflictException() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        doThrow(new BusinessConflictException("active contracts"))
                .when(contractService).checkAndAllActiveContractsByClient(client);

        // when
        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> serviceUnderTest.deleteUser(1L)
        );

        // then
        assertThat(exception.getMessage()).contains("It is not possible to delete an account while it exists active contracts");

        verify(clientRepository).findById(1L);
        verify(contractService).checkAndAllActiveContractsByClient(client);
        verify(clientRepository, never()).save(any(Client.class));
    }




    @Test
    @DisplayName("Test delete user with non-existing user throws exception")
    public void givenNonExistingUserId_whenDeleteUser_thenThrowException() {
        // given
        given(clientRepository.findById(1L)).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.deleteUser(1L)
        );

        verify(clientRepository).findById(1L);
        verify(contractService, never()).checkAndAllActiveContractsByClient(any());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Test delete user with active contracts throws exception")
    public void givenUserWithActiveContracts_whenDeleteUser_thenThrowException() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        doThrow(new BusinessConflictException("active contracts"))
                .when(contractService).checkAndAllActiveContractsByClient(client);

        // when + then
        assertThrows(
                BusinessConflictException.class,
                () -> serviceUnderTest.deleteUser(1L)
        );

        verify(clientRepository).findById(1L);
        verify(contractService).checkAndAllActiveContractsByClient(client);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Test update role functionality")
    public void givenUserIdAndRoleName_whenUpdateRole_thenRoleIsUpdated() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        Role oldRole = dataUtils.getRolePersisted();
        client.setRole(oldRole);

        Role newRole = Role.builder().id(2L).name("ADMIN").build();

        AllUserResponse response = AllUserResponse.builder()
                .id(1L)
                .login("testuser")
                .email("test@example.com")
                .roleName("ADMIN")
                .banned(false)
                .build();

        given(roleService.getRoleByName("ADMIN")).willReturn(newRole);
        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(clientRepository.save(client)).willReturn(client);
        given(clientMapper.toDtoForAdmin(client)).willReturn(response);

        // when
        AllUserResponse actual = serviceUnderTest.updateRole(1L, "ADMIN");

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.roleName()).isEqualTo("ADMIN");

        verify(roleService).getRoleByName("ADMIN");
        verify(clientRepository).findById(1L);
        verify(clientRepository).save(client);
        verify(clientMapper).toDtoForAdmin(client);
    }


    @Test
    @DisplayName("Test update role with non-existing user throws exception")
    public void givenNonExistingUserId_whenUpdateRole_thenThrowException() {
        // given
        given(clientRepository.findById(99L)).willReturn(Optional.empty());
        given(roleService.getRoleByName("ADMIN"))
                .willReturn(Role.builder().id(1L).name("ADMIN").build());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.updateRole(99L, "ADMIN")
        );

        verify(roleService).getRoleByName("ADMIN");
        verify(clientRepository).findById(99L);
        verify(clientRepository, never()).save(any(Client.class));
    }


    @Test
    @DisplayName("Test filter users functionality")
    public void givenFilterRequest_whenFilterUsers_thenPageIsReturned() {
        // given
        FilterUserRequest filter = new FilterUserRequest(false, "CLIENT");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        Client client1 = dataUtils.createAndSaveClient("user1", "user1@example.com", false);
        Client client2 = dataUtils.createAndSaveClient("user2", "user2@example.com", false);

        List<Client> clients = List.of(client1, client2);
        Page<Client> clientPage = new PageImpl<>(clients, pageable, clients.size());

        ShortUserResponse response1 = new ShortUserResponse(1L, "user1", "user1@example.com", "CLIENT", false, false);
        ShortUserResponse response2 = new ShortUserResponse(2L, "user2", "user2@example.com", "CLIENT", false, false);

        given(clientRepository.findByFilter(false, "CLIENT", pageable)).willReturn(clientPage);
        given(clientMapper.toShortDtoForAdmin(client1)).willReturn(response1);
        given(clientMapper.toShortDtoForAdmin(client2)).willReturn(response2);

        // when
        Page<ShortUserResponse> actual = serviceUnderTest.filterUsers(filter, pageable);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getContent()).hasSize(2);
        assertThat(actual.getContent().get(0).login()).isEqualTo("user1");
        assertThat(actual.getContent().get(1).login()).isEqualTo("user2");

        verify(clientRepository).findByFilter(false, "CLIENT", pageable);
    }


    @Test
    @DisplayName("Test filter users supports sort by email and login")
    public void givenValidSortFields_whenFilterUsers_thenDoesNotThrowException() {
        // given
        FilterUserRequest filter = new FilterUserRequest(false, "CLIENT");
        Pageable pageableByEmail = PageRequest.of(0, 5, Sort.by("email"));
        Pageable pageableByLogin = PageRequest.of(0, 5, Sort.by("login"));

        Page<Client> emptyPage = Page.empty(pageableByEmail);

        given(clientRepository.findByFilter(false, "CLIENT", pageableByEmail)).willReturn(emptyPage);
        given(clientRepository.findByFilter(false, "CLIENT", pageableByLogin)).willReturn(emptyPage);

        // when
        Page<ShortUserResponse> resultEmail = serviceUnderTest.filterUsers(filter, pageableByEmail);
        Page<ShortUserResponse> resultLogin = serviceUnderTest.filterUsers(filter, pageableByLogin);

        // then
        assertThat(resultEmail).isNotNull();
        assertThat(resultLogin).isNotNull();

        verify(clientRepository).findByFilter(false, "CLIENT", pageableByEmail);
        verify(clientRepository).findByFilter(false, "CLIENT", pageableByLogin);
    }


    @Test
    @DisplayName("Test filter users with null fields in filter works correctly")
    public void givenNullFilterFields_whenFilterUsers_thenPageReturned() {
        // given
        FilterUserRequest filter = new FilterUserRequest(null, null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        Page<Client> emptyPage = Page.empty(pageable);
        given(clientRepository.findByFilter(null, null, pageable)).willReturn(emptyPage);

        // when
        Page<ShortUserResponse> result = serviceUnderTest.filterUsers(filter, pageable);

        // then
        assertThat(result).isNotNull();
        verify(clientRepository).findByFilter(null, null, pageable);
    }




    @Test
    @DisplayName("Test filter users returns empty page when no users match filter")
    public void givenFilterRequest_whenNoUsersFound_thenEmptyPageIsReturned() {
        // given
        FilterUserRequest filter = new FilterUserRequest(false, "CLIENT");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        Page<Client> emptyPage = Page.empty(pageable);
        given(clientRepository.findByFilter(false, "CLIENT", pageable)).willReturn(emptyPage);

        // when
        Page<ShortUserResponse> actual = serviceUnderTest.filterUsers(filter, pageable);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getContent()).isEmpty();

        verify(clientRepository).findByFilter(false, "CLIENT", pageable);
    }


    @Test
    @DisplayName("Test filter users with invalid sort field throws exception")
    public void givenFilterRequestWithInvalidSort_whenFilterUsers_thenThrowException() {
        // given
        FilterUserRequest filter = new FilterUserRequest(false, "CLIENT");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("invalidField"));

        // when + then
        assertThrows(
                InvalidQueryParameterException.class,
                () -> serviceUnderTest.filterUsers(filter, pageable)
        );

        verify(clientRepository, never()).findByFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Test get entity by id functionality")
    public void givenUserId_whenGetEntity_thenClientIsReturned() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));

        // when
        Client actual = serviceUnderTest.getEntity(1L);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getLogin()).isEqualTo("testuser");

        verify(clientRepository).findById(1L);
    }

    @Test
    @DisplayName("Test get entity by id with non-existing user throws exception")
    public void givenNonExistingUserId_whenGetEntity_thenThrowException() {
        // given
        given(clientRepository.findById(1L)).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.getEntity(1L),

                "Client not found"
        );

        verify(clientRepository).findById(1L);
    }

    @Test
    @DisplayName("Test change password functionality")
    public void givenCorrectOldPassword_whenChangePassword_thenPasswordIsChanged() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        client.setPassword("oldEncodedPassword");

        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(passwordEncoder.matches("oldPassword", "oldEncodedPassword")).willReturn(true);
        given(passwordEncoder.encode("newPassword")).willReturn("newEncodedPassword");
        given(clientRepository.save(client)).willReturn(client);

        // when
        serviceUnderTest.changePassword(1L, request);

        // then
        verify(clientRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", "oldEncodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(clientRepository).save(client);
    }


    @Test
    @DisplayName("Test change password actually updates encoded password")
    public void givenValidPasswordChange_whenChangePassword_thenPasswordIsUpdated() {
        // given
        Client client = dataUtils.createAndSaveClient("changeuser", "change@example.com");
        client.setId(1L);
        client.setPassword("oldEncoded");

        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(passwordEncoder.matches("oldPassword", "oldEncoded")).willReturn(true);
        given(passwordEncoder.encode("newPassword")).willReturn("newEncoded");
        given(clientRepository.save(client)).willReturn(client);

        // when
        serviceUnderTest.changePassword(1L, request);

        // then
        assertThat(client.getPassword()).isEqualTo("newEncoded");

        verify(passwordEncoder).encode("newPassword");
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("Test change password when user not found throws exception")
    public void givenNonExistingUserId_whenChangePassword_thenThrowException() {
        // given
        ChangePasswordRequest request = new ChangePasswordRequest("old", "new");
        given(clientRepository.findById(1L)).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.changePassword(1L, request)
        );

        verify(clientRepository).findById(1L);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(clientRepository, never()).save(any(Client.class));
    }


    @Test
    @DisplayName("Test change password with incorrect old password throws exception")
    public void givenIncorrectOldPassword_whenChangePassword_thenThrowException() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        client.setPassword("oldEncodedPassword");

        ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword");

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(passwordEncoder.matches("wrongPassword", "oldEncodedPassword")).willReturn(false);

        // when + then
        assertThrows(
                PasswordException.class,
                () -> serviceUnderTest.changePassword(1L, request),
                "Incorrect password"
        );

        verify(clientRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", "oldEncodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Test update profile functionality")
    public void givenUpdateProfileRequest_whenUpdateProfile_thenProfileIsUpdated() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        client.setFirstName("OldFirst");
        client.setLastName("OldLast");
        client.setPhone("111111");

        UpdateProfileRequest request = new UpdateProfileRequest("NewFirst", "NewLast", "222222");

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(clientRepository.existsByPhoneAndIdNot("222222", 1L)).willReturn(false);
        given(clientRepository.save(client)).willReturn(client);

        // when
        serviceUnderTest.updateProfile(1L, request);

        // then
        verify(clientRepository).findById(1L);
        verify(clientRepository).existsByPhoneAndIdNot("222222", 1L);
        verify(clientRepository).save(client);
    }


    @Test
    @DisplayName("Test update profile with all null fields does nothing and does not fail")
    public void givenAllNullFields_whenUpdateProfile_thenNoFieldsUpdated() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        client.setFirstName("OldFirst");
        client.setLastName("OldLast");
        client.setPhone("111111");

        UpdateProfileRequest request = new UpdateProfileRequest(null, null, null);

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(clientRepository.save(client)).willReturn(client);

        // when
        serviceUnderTest.updateProfile(1L, request);

        // then
        assertThat(client.getFirstName()).isEqualTo("OldFirst");
        assertThat(client.getLastName()).isEqualTo("OldLast");
        assertThat(client.getPhone()).isEqualTo("111111");

        verify(clientRepository).findById(1L);
        verify(clientRepository, never()).existsByPhoneAndIdNot(anyString(), anyLong());
        verify(clientRepository).save(client);
    }


    @Test
    @DisplayName("Test update profile when user not found throws exception")
    public void givenNonExistingUserId_whenUpdateProfile_thenThrowException() {
        // given
        UpdateProfileRequest request = new UpdateProfileRequest("New", "Name", "999999");
        given(clientRepository.findById(1L)).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.updateProfile(1L, request)
        );

        verify(clientRepository).findById(1L);
        verify(clientRepository, never()).save(any(Client.class));
    }


    @Test
    @DisplayName("Test update profile with duplicate phone throws exception")
    public void givenUpdateProfileRequestWithDuplicatePhone_whenUpdateProfile_thenThrowException() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        client.setPhone("111111");

        UpdateProfileRequest request = new UpdateProfileRequest("NewFirst", "NewLast", "222222");

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(clientRepository.existsByPhoneAndIdNot("222222", 1L)).willReturn(true);

        // when + then
        assertThrows(
                AlreadyExistsException.class,
                () -> serviceUnderTest.updateProfile(1L, request)
        );

        verify(clientRepository).findById(1L);
        verify(clientRepository).existsByPhoneAndIdNot("222222", 1L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Test update profile with null fields does not update them")
    public void givenUpdateProfileRequestWithNullFields_whenUpdateProfile_thenOnlyNonNullFieldsAreUpdated() {
        // given
        Client client = dataUtils.createAndSaveClient("testuser", "test@example.com");
        client.setId(1L);
        client.setFirstName("OldFirst");
        client.setLastName("OldLast");
        client.setPhone("111111");

        UpdateProfileRequest request = new UpdateProfileRequest(null, "NewLast", null);

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(clientRepository.save(client)).willReturn(client);

        // when
        serviceUnderTest.updateProfile(1L, request);

        // then
        verify(clientRepository).findById(1L);
        verify(clientRepository, never()).existsByPhoneAndIdNot(anyString(), anyLong());
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("Test ban user with non-existing user throws exception")
    public void givenNonExistingUserId_whenBanUser_thenThrowException() {
        // given
        given(clientRepository.findById(1L)).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.banUser(1L)
        );

        verify(clientRepository).findById(1L);
        verify(clientRepository, never()).save(any(Client.class));
    }


    @Test
    @DisplayName("Test ban and unban change banned flag correctly before save")
    public void givenUser_whenBanAndUnban_thenBannedFlagChangesProperly() {
        // given
        Client client = dataUtils.createAndSaveClient("banuser", "ban@example.com");
        client.setId(1L);
        client.setBanned(false);

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(clientRepository.save(client)).willReturn(client);
        given(clientMapper.toDtoForAdmin(client)).willReturn(AllUserResponse.builder()
                .id(1L)
                .login("banuser")
                .email("ban@example.com")
                .roleName("CLIENT")
                .banned(true)
                .build());

        // when
        serviceUnderTest.banUser(1L);

        // then
        assertThat(client.isBanned()).isTrue();

        // given
        client.setBanned(true);
        given(clientMapper.toDtoForAdmin(client)).willReturn(AllUserResponse.builder()
                .id(1L)
                .login("banuser")
                .email("ban@example.com")
                .roleName("CLIENT")
                .banned(false)
                .build());

        // when
        serviceUnderTest.unbanUser(1L);

        // then
        assertThat(client.isBanned()).isFalse();
        verify(clientRepository, times(2)).save(client);
    }

    @Test
    @DisplayName("Test unban user with non-existing user throws exception")
    public void givenNonExistingUserId_whenUnbanUser_thenThrowException() {
        // given
        given(clientRepository.findById(1L)).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.unbanUser(1L)
        );

        verify(clientRepository).findById(1L);
        verify(clientRepository, never()).save(any(Client.class));
    }
}
