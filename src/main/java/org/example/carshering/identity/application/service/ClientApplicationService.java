package org.example.carshering.identity.application.service;

import jakarta.validation.Valid;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.identity.api.dto.response.ResetPasswordResponse;
import org.example.carshering.identity.api.dto.request.*;
import org.example.carshering.identity.api.dto.response.VerifyStatusResponse;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.application.dto.response.DocumentDto;
import org.example.carshering.identity.application.dto.response.RoleDto;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ClientApplicationService {

    @Transactional
    ClientDto createUser(@Valid RegistrationRequest createUserRequest);


    ClientDto findUser(Long userId);

    Page<ClientDto> filterUsers(FilterUserRequest filter, Pageable pageable);

    void deleteUser(Long userId);

    ClientDto banUser(Long userId);

    ClientDto unbanUser(Long userId);

    VerifyStatusResponse sendForVerifyEmail(Long userId);

    ClientDto verifyEmail(ClientId clientId);


    @Transactional
    void updateProfile(Long userId, UpdateProfileRequest request);

    @Transactional
    ClientDto updateRole(Long userId, Long roleId);

    @Transactional
    ClientDto updateRoleByName(Long userId, String roleName);

    Page<ClientDto> filterUsersByRoleName(Boolean banned, String roleName, Pageable pageable);


    // email password reset
    ResetPasswordResponse resetPasswordForEmail(@Valid ResetPasswordRequest request);

    @Transactional
    void updatePasswordForEmail(ClientId clientId, String password);

    @Transactional
    void changePassword(Long userId, ChangePasswordRequest request);

    @Transactional
    void resetPassword(String code, NewPasswordRequest request);

    // document use case

    DocumentDto createDocument(@Valid CreateDocumentRequest createDocumentRequest, Long userId);

    boolean hasDocument(Long userId);

    DocumentDto findDocument(Long userId);

    DocumentDto findValidDocument(Long userId);

    DocumentDto updateDocument(Long userId, UpdateDocumentRequest request);


    void verifyDocument(Long documentId);

    Page<DocumentDto> getAllDocuments(boolean onlyUnverified, Pageable pageable);

    void deleteDocument(Long userId);

    Optional<ClientDto> findUserByLogin(String username);

    Optional<RoleDto> findRoleByRoleId(Long aLong);
}
