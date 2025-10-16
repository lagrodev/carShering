package org.example.carshering.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.request.UpdateProfileRequest;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.entity.Client;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface ClientService {
    UserResponse findUser(Long userId);
    void banUser(Long userId);

    void deleteUser(Long userId);

    void unbanUser(Long userId);
    UserResponse createUser(@Valid RegistrationRequest createUserRequest);
    Client getEntity(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);

    void updateProfile(Long userId, UpdateProfileRequest request);

    List<UserResponse> getAllUsers();

    void updateRole(Long userId, String roleName);

    List<UserResponse> filterUsers(Boolean banned, String roleName, String sortBy, String sortOrder);
}
