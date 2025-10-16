package org.example.carshering.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.request.UpdateProfileRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserRepositoryCustom userRepositoryCustom;
    private final ClientMapper clientMapper;


    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Override
    public UserResponse findUser(Long userId) {
        return clientMapper.toDto(getEntity(userId));
    }


    @Override
    public void banUser(Long userId) {
        Client client = getEntity(userId);
        client.setBanned(true);
        clientRepository.save(client);
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
    public void unbanUser(Long userId) {
        Client client = getEntity(userId);
        client.setBanned(false);
        clientRepository.save(client);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toDto)
                .toList();
    }

    @Override
    public void updateRole(Long userId, String roleName) {
        Role role = roleService.getRoleByName(roleName);
        clientRepository.findById(userId).ifPresent(client -> {
            client.setRole(role);
            clientRepository.save(client);
        });

    }

    @Override
    public List<UserResponse> filterUsers(Boolean banned, String roleName, String sortBy, String sortOrder) {
        String safeSortBy = switch (sortBy) {
            case "id", "email", "login" -> sortBy;
            default -> "id";
        };

        List<Client> clients = clientRepository.findByFilter(banned, roleName, safeSortBy);

        return clients.stream()
                .map(clientMapper::toDto)
                .toList();
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
