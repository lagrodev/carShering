package org.example.carshering.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.dto.request.FilterUserRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.request.UpdateProfileRequest;
import org.example.carshering.dto.response.AllUserResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Role;
import org.example.carshering.exceptions.PasswordException;
import org.example.carshering.exceptions.UserAlreadyExistsException;
import org.example.carshering.mapper.ClientMapper;
import org.example.carshering.repository.ClientRepository;
import org.example.carshering.repository.UserRepositoryCustom;
import org.example.carshering.service.ClientService;
import org.example.carshering.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
//    private final UserRepositoryCustom userRepositoryCustom;
    private final ClientMapper clientMapper;


    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Override
    public AllUserResponse findAllUser(Long userId) {
        return clientMapper.toDtoForAdmin(getEntity(userId));
    }

    @Override
    public UserResponse findUser(Long userId) {
        return clientMapper.toDto(getEntity(userId));
    }


    @Override
    public AllUserResponse banUser(Long userId) {
        Client client = getEntity(userId);
        client.setBanned(true);
        clientRepository.save(client);
        return clientMapper.toDtoForAdmin(client);

    }

    @Override
    public void deleteUser(Long userId) {
        Client client = clientRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        client.setDeleted(true);
        client.setBanned(true);
        clientRepository.save(client);
    }

    @Override
    public AllUserResponse unbanUser(Long userId) {
        Client client = getEntity(userId);
        client.setBanned(false);
        clientRepository.save(client);
        return clientMapper.toDtoForAdmin(client);
    }



    @Override
    public AllUserResponse updateRole(Long userId, String roleName) {
        Role role = roleService.getRoleByName(roleName);
        clientRepository.findById(userId).ifPresent(client -> {
            client.setRole(role);
            clientRepository.save(client);
        });
        return clientMapper.toDtoForAdmin(getEntity(userId));

    }

    @Override
    public Page<AllUserResponse> filterUsers(FilterUserRequest filter, Pageable pageable) {

        var availableBySort = Set.of("id", "email", "login");

        var sort = pageable.getSort();

        for (var order : sort) {
            if (!availableBySort.contains(order.getProperty())) {
                throw new IllegalArgumentException("Недопустимое поле сортировки: " + order.getProperty());
            }
        }



        return clientRepository.findByFilter(filter.banned(), filter.roleName(), pageable).map(clientMapper::toDtoForAdmin);
    }

    @Override
    public UserResponse createUser(RegistrationRequest request) {
        if (clientRepository.existsByLoginAndDeletedFalse(request.login())) {
            throw new UserAlreadyExistsException("Login уже используется");
        }
        if (clientRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new UserAlreadyExistsException("Email уже зарегистрирован");
        }

        Client client = clientMapper.toEntity(request);
        client.setPassword(passwordEncoder.encode(client.getPassword()));

        client.setRole(roleService.getRoleByName("USER"));

        return clientMapper.toDto(clientRepository.save(client));
    }

    @Override
    public Client getEntity(Long userId) {
        return clientRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        Client client = getEntity(userId);

        if (!passwordEncoder.matches(request.oldPassword(), client.getPassword())) {
            throw new PasswordException("Неверный старый пароль");
        }

        client.setPassword(passwordEncoder.encode(request.newPassword()));
        clientRepository.save(client);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        Client client = getEntity(userId);

        if (request.firstName() != null) client.setFirstName(request.firstName());
        if (request.lastName() != null) client.setLastName(request.lastName());
        if (request.phone() != null) client.setPhone(request.phone());

        clientRepository.save(client);
    }




}
