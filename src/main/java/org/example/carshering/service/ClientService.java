package org.example.carshering.service;

import jakarta.validation.Valid;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.dto.request.FilterUserRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.request.update.UpdateProfileRequest;
import org.example.carshering.dto.response.AllUserResponse;
import org.example.carshering.dto.response.ShortUserResponse;
import org.example.carshering.dto.response.UserResponse;
import org.example.carshering.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {
    AllUserResponse findAllUser(Long userId);
    UserResponse findUser(Long userId);
    AllUserResponse banUser(Long userId);

    void deleteUser(Long userId);

    AllUserResponse unbanUser(Long userId);
    UserResponse createUser(@Valid RegistrationRequest createUserRequest);
    Client getEntity(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);

    void updateProfile(Long userId, UpdateProfileRequest request);

    AllUserResponse updateRole(Long userId, String roleName);

    Page<ShortUserResponse> filterUsers(FilterUserRequest filter, Pageable pageable);
}
