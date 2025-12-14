//package org.example.carshering.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import org.example.carshering.common.exceptions.custom.*;
//import org.example.carshering.identity.api.dto.response.AllUserResponse;
//import org.example.carshering.identity.api.dto.response.ShortUserResponse;
//import org.example.carshering.identity.api.dto.response.UserResponse;
//import org.example.carshering.identity.api.dto.response.VerifyStatusResponse;
//import org.example.carshering.identity.domain.valueobject.user.Password;
//import org.example.carshering.identity.domain.valueobject.user.Phone;
//import org.example.carshering.dto.request.ChangePasswordRequest;
//import org.example.carshering.identity.api.dto.request.FilterUserRequest;
//import org.example.carshering.identity.api.dto.request.RegistrationRequest;
//import org.example.carshering.identity.api.dto.request.ResetPasswordRequest;
//import org.example.carshering.identity.api.dto.request.UpdateProfileRequest;
//import org.example.carshering.dto.response.*;
//import org.example.carshering.identity.infrastructure.persistence.entity.Client;
//import org.example.carshering.identity.infrastructure.persistence.entity.ClientJpaEntity;
//import org.example.carshering.identity.infrastructure.persistence.entity.Role;
//import org.example.carshering.identity.api.mapper.ClientMapper;
//import org.example.carshering.identity.infrastructure.persistence.repository.ClientRepository;
//import org.example.carshering.service.interfaces.ClientService;
//import org.example.carshering.identity.infrastructure.email.EmailService;
//import org.example.carshering.service.domain.ContractServiceHelper;
//import org.example.carshering.service.domain.RoleServiceHelper;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//public class ClientServiceImpl implements ClientService {
//
//
//    private final ClientMapper clientMapper;
//
//    private final PasswordEncoder passwordEncoder;
//
//    private final RoleServiceHelper roleService;
//    private final ContractServiceHelper contractService;
//    private final ClientRepository clientRepository;
//    private final EmailService emailService;
//
//    @Override
//    public AllUserResponse findAllUser(Long userId) {
//        return clientMapper.toDtoForAdmin(getEntity(userId));
//    }
//
//    @Override
//    public UserResponse findUser(Long userId) {
//        return clientMapper.toDto(clientRepository.findById(userId)
//                .orElse(null));
//    }
//
//    @Override
//    public AllUserResponse banUser(Long userId) {
//        Client client = getEntity(userId);
//        client.ban();
//        clientRepository.save(client);
//        return clientMapper.toDtoForAdmin(client);
//
//    }
//
//    // todo отменить все активные бронирования и аренды при удалении пользователя... готово по идеи
//    @Override
//    public void deleteUser(Long userId) {
//        Client client = clientRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//
//        try {
//            contractService.checkAndAllActiveContractsByClient(client);
//        } catch (BusinessConflictException e) {
//            throw new BusinessConflictException("It is not possible to delete an account while it exists " + e.getMessage());
//        }
//
//        client.delete();
//        clientRepository.save(client);
//    }
//
//    @Override
//    public AllUserResponse unbanUser(Long userId) {
//        Client client = getEntity(userId);
//        client.unban();
//        clientRepository.save(client);
//        return clientMapper.toDtoForAdmin(client);
//    }
//
//    @Override
//    @Transactional
//    public AllUserResponse updateRole(Long userId, String roleName) {
//        Role role = roleService.getRoleByName(roleName);
//        Client client = clientRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("Client not found with id " + userId));
//
//        client.setRole(role);
//        clientRepository.save(client);
//
//        return clientMapper.toDtoForAdmin(client);
//    }
//
//    @Override
//    public Page<ShortUserResponse> filterUsers(FilterUserRequest filter, Pageable pageable) {
//
//        var availableBySort = Set.of("id", "email", "login", "banned", "role.name");
//
//        var sort = pageable.getSort();
//
//        for (var order : sort) {
//            if (!availableBySort.contains(order.getProperty())) {
//                throw new InvalidQueryParameterException(order.getProperty());
//            }
//        }
//
//
//        return clientRepository.findByFilter(filter.banned(), filter.roleName(), pageable)
//                .map(clientMapper::toShortDtoForAdmin);
//    }
//
//    @Override
//    @Transactional
//    public VerifyStatusResponse verifyEmail(Long userId) {
//        Client client = clientRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("Client not found with id %s", String.valueOf(userId))); // todo вопрос, а  надо ли это исключение? я уже в аккаунте => я априоре есть в бд....
//
//        if (client.isEmailVerified()) {
//            throw new RuntimeException("email is already verified"); // todo castom
//        }
//
//        emailService.sendVerificationEmail(client);
//
//        return new VerifyStatusResponse("VERIFICATION_EMAIL_SENT", "Verification email sent to " + client.getEmail());
//
//    }
//
//    @Override
//    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
//        Client client = clientRepository.findByEmailAndDeletedFalse(request.email()).orElseThrow(
//                () -> new NotFoundException("Client not found with id %s", request.email())
//        );
//
//        if (!client.isEmailVerified()) {
//            emailService.sendVerificationEmail(client);
//            throw new EmailNotVerifiedException("We can't support for help this account, you need to verified email. We have sent a new email for verification");
//        }
//        emailService.sendResetPasswordEmail(client);
//
//        return new ResetPasswordResponse("RESET_EMAIL_SENT", "Reset password email sent to " + client.getEmail());
//    }
//
//    @Transactional
//    @Override
//    public void updatePassword(Client client, String password) {
//        client.setPassword(Password.ofEncoded(passwordEncoder.encode(password)));
//        clientRepository.save(client); // todo при смене пароля все сессии юзера должны быть инвалидированы, но это только после того, как появится рефреш токен
//        emailService.sendPasswordResetConfirmationEmail(client);
//    }
//
//
//
//    @Override
//    public UserResponse createUser(RegistrationRequest request) {
//        if (clientRepository.existsByLoginAndDeletedFalse(request.login())) {
//            throw new AlreadyExistsException("Login already in use");
//        }
//        if (clientRepository.existsByEmailAndDeletedFalse(request.email())) {
//            throw new AlreadyExistsException("Email already in use");
//        }
//
//        Client client = clientMapper.toEntity(request);
//        client.setPassword(Password.ofEncoded(passwordEncoder.encode(client.getPassword().getValue())));
//
//        client.setRole(roleService.getRoleByName("CLIENT"));
//
//        return clientMapper.toDto(clientRepository.save(client));
//    }
//
//    @Override
//    public ClientJpaEntity getEntity(Long userId) {
//        return clientRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("Client not found"));
//    }
//
//    @Override
//    @Transactional
//    public void changePassword(Long userId, ChangePasswordRequest request) {
//        Client client = getEntity(userId);
//
//        if (!passwordEncoder.matches(request.oldPassword(), client.getPassword().getValue())) {
//            throw new PasswordException("Incorrect password");
//        }
//        updatePassword(client, request.newPassword());
//    }
//
//
//
//    @Override
//    public void updateProfile(Long userId, UpdateProfileRequest request) {
//        Client client = getEntity(userId);
//
//        if (request.firstName() != null) client.setFirstName(request.firstName());
//        if (request.lastName() != null) client.setLastName(request.lastName());
//        if (request.phone() != null) {
//            if (!clientRepository.existsByPhoneAndIdNot(request.phone(), userId)) {
//                client.setPhone(Phone.of( request.phone()));
//            } else throw new AlreadyExistsException("This phone is assigned to another account");
//        }
//        clientRepository.save(client);
//
//    }
//
//
//}
