package org.example.carshering.dto.request;

public record UpdateProfileRequest (
        String firstName,
        String lastName,
        String oldPassword,
        String newPassword,
        String phone

){
}
