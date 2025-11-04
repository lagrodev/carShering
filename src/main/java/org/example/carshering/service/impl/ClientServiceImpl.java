package org.example.carshering.service.impl;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.dto.request.FilterUserRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.request.update.UpdateProfileRequest;
import org.example.carshering.dto.response.AllUserResponse;
import org.example.carshering.dto.response.ShortUserResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Role;
import org.example.carshering.exceptions.custom.AlreadyExistsException;
import org.example.carshering.exceptions.custom.BusinessConflictException;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.exceptions.custom.PasswordException;
import org.example.carshering.mapper.ClientMapper;
import org.example.carshering.repository.ClientRepository;
import org.example.carshering.service.ClientService;
import org.example.carshering.service.ContractService;
import org.example.carshering.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;

    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final ContractService contractService;

    @Override
    public AllUserResponse findAllUser(Long userId) {
        return clientMapper.toDtoForAdmin(getEntity(userId));
    }

    @Override
    public UserResponse findUser(Long userId) {
        return clientMapper.toDto(clientRepository.findById(userId)
                .orElse(null));
    }

    @Override
    public AllUserResponse banUser(Long userId) {
        Client client = getEntity(userId);
        client.setBanned(true);
        clientRepository.save(client);
        return clientMapper.toDtoForAdmin(client);

    }

    // todo отменить все активные бронирования и аренды при удалении пользователя... готово по идеи
    @Override
    public void deleteUser(Long userId) {
        Client client = clientRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        try {
            contractService.checkAndAllActiveContractsByClient(client);
        } catch (BusinessConflictException e) {
            throw new BusinessConflictException("It is not possible to delete an account while it exists " + e.getMessage());
        }

        client.setDeleted(true);
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
    @Transactional
    public AllUserResponse updateRole(Long userId, String roleName) {
        Role role = roleService.getRoleByName(roleName);
        Client client = clientRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Client not found with id " + userId));

        client.setRole(role);
        clientRepository.save(client);

        return clientMapper.toDtoForAdmin(client);
    }



    @Override
    public Page<ShortUserResponse> filterUsers(FilterUserRequest filter, Pageable pageable) {

        var availableBySort = Set.of("id", "email", "login");

        var sort = pageable.getSort();

        for (var order : sort) {
            if (!availableBySort.contains(order.getProperty())) {
                throw new IllegalArgumentException("Недопустимое поле сортировки: " + order.getProperty());
            }
        }


        return clientRepository.findByFilter(filter.banned(), filter.roleName(), pageable)
                .map(clientMapper::toShortDtoForAdmin);
    }


    @Override
    public UserResponse createUser(RegistrationRequest request) {
        if (clientRepository.existsByLoginAndDeletedFalse(request.login())) {
            throw new AlreadyExistsException("Login уже используется");
        }
        if (clientRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new AlreadyExistsException("Email уже зарегистрирован");
        }

        Client client = clientMapper.toEntity(request);
        client.setPassword(passwordEncoder.encode(client.getPassword()));

        client.setRole(roleService.getRoleByName("CLIENT"));

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
        if (request.phone() != null) {
            if (!clientRepository.existsByPhoneAndIdNot(request.phone(), userId)) {
                client.setPhone(request.phone());
            } else throw new ValidationException("Данный телефон закреплен за другим аккаунтом");
        }


        clientRepository.save(client);

    }


}
