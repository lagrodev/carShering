package org.example.carshering.identity.infrastructure.email;


import org.example.carshering.identity.api.dto.request.NewPasswordRequest;
import org.example.carshering.identity.domain.model.Client;
import org.springframework.transaction.annotation.Transactional;

public interface EmailService {


    void sendVerificationEmail(Client client);

    @Transactional
        // это для почты... :\ мне не нравится что это тут
        // todo подумать, может лучше перенести это отсюда..
    void verifyToken(String code);

    void sendResetPasswordEmail(Client client);

    void sendPasswordResetConfirmationEmail(Client user);

    void resetPassword(String code, NewPasswordRequest request);
}
