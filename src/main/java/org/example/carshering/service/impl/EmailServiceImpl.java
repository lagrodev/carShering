package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.domain.entity.Client;
import org.example.carshering.domain.entity.VerificationCode;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.repository.CodeRepository;
import org.example.carshering.service.interfaces.ClientDetailsService;
import org.example.carshering.service.interfaces.EmailService;
import org.example.carshering.utils.VerificationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final CodeRepository codeRepository;
    @Value("${frontend.port}")
    private String port;
    @Value("${frontend.address}")
    private String address;

    @Override
    public void sendVerificationEmail(Client client) {
        String token = generateVerificationToken(client);
        sendVerificationEmail(client, token);
    }

    private String generateVerificationToken(Client client) {
        String code = VerificationToken.generateSecretString();
        VerificationCode verificationCode = VerificationCode.builder()
                .type(VerificationCode.VerificationCodeType.EMAIL_VERIFICATION)
                .code(code)
                .client(client)
                .build();
        codeRepository.save(verificationCode);
        return code;
    }
    private final ClientDetailsService clientDetailsService;

    @Transactional
    @Override
    public void verifyToken(String code) {

        VerificationCode verificationCode = codeRepository
                .findByCodeAndTypeIs(
                        code, VerificationCode.VerificationCodeType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new NotFoundException("Token not found")); // TODO: custom exception

        boolean isExpired = verificationCode.getCreatedAt()
                .plus(15, ChronoUnit.MINUTES)
                .isBefore(Instant.now());

        if (isExpired) {
            throw new RuntimeException("Time life token has passed");
        }

        Client client = verificationCode.getClient();

        clientDetailsService.setEmailVerified(client);
        codeRepository.delete(verificationCode); // TODO: use a service method for this
    }



    public void sendVerificationEmail(Client client, String verificationCode) {
        // TODO: customize email content from configuration/template
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(client.getEmail().getValue());
        msg.setSubject("Email Verification");
        String url = "http://" + address + ":" + port + "/verify?code=" + verificationCode;
        msg.setText("Please verify your email by clicking the following link: " + url +
                "\n"+
                "If you did not request this, please ignore this email.");
        mailSender.send(msg);
    }

    @Override
    public void sendResetPasswordEmail(Client client) {
        String token = generateResetPasswordToken(client);
        sendPasswordResetConfirmationEmail(client, token);
    }

    @Override
    public void sendPasswordResetConfirmationEmail(Client user) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail().getValue());
        msg.setSubject("Password Reset Confirmation");
        msg.setText("Your password has been successfully reset.");
        mailSender.send(msg);
    }

    private void sendPasswordResetConfirmationEmail(Client client, String token) {
        // TODO: customize email content from configuration/template
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(client.getEmail().getValue());
        msg.setSubject("Email Verification");
        String url = "http://" + address + ":" + port + "/reset?code=" + token;
        msg.setText("Please reset your password by clicking the following link: " + url);
        mailSender.send(msg);
    }


    private String generateResetPasswordToken(Client client) {
        String code = VerificationToken.generateSecretString();

        VerificationCode resetPasswordCode = VerificationCode.builder()
                .client(client)
                .type(VerificationCode.VerificationCodeType.PASSWORD_RESET)
                .code(code)
                .build();

        codeRepository.save(resetPasswordCode);

        return code;
    }
}
