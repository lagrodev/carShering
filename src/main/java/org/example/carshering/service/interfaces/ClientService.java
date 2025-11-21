package org.example.carshering.service.interfaces;

import jakarta.validation.Valid;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.dto.request.FilterUserRequest;
import org.example.carshering.dto.request.RegistrationRequest;
import org.example.carshering.dto.request.ResetPasswordRequest;
import org.example.carshering.dto.response.*;
import org.example.carshering.dto.request.update.UpdateProfileRequest;
import org.example.carshering.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface ClientService {
    AllUserResponse findAllUser(Long userId);
    UserResponse findUser(Long userId);
    AllUserResponse banUser(Long userId);

    void deleteUser(Long userId);

    AllUserResponse unbanUser(Long userId);

    @Transactional
    UserResponse createUser(@Valid RegistrationRequest createUserRequest);
    Client getEntity(Long userId);

    @Transactional
    void changePassword(Long userId, ChangePasswordRequest request);


    @Transactional
    void updatePassword(Client client, String password);

    @Transactional
    void updateProfile(Long userId, UpdateProfileRequest request);

    @Transactional
    AllUserResponse updateRole(Long userId, String roleName);

    Page<ShortUserResponse> filterUsers(FilterUserRequest filter, Pageable pageable);

    @Transactional
    VerifyStatusResponse verifyEmail(Long userId);

    ResetPasswordResponse resetPassword(@Valid ResetPasswordRequest request);

}
