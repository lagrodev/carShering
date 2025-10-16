package org.example.carshering.dto.request;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {}
