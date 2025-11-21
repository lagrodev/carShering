package org.example.carshering.service.interfaces;


import org.example.carshering.entity.Client;

public interface EmailService {
    void sendVerificationEmail(Client client);

    void sendResetPasswordEmail(Client client);

    void sendPasswordResetConfirmationEmail(Client user);
}
