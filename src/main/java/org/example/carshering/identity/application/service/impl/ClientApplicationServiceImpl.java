package org.example.carshering.identity.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.common.exceptions.custom.BusinessException;
import org.example.carshering.common.exceptions.custom.EmailNotVerifiedException;
import org.example.carshering.common.exceptions.custom.NotFoundException;
import org.example.carshering.fleet.domain.valueobject.DateOfIssue;
import org.example.carshering.fleet.domain.valueobject.IssuingAuthority;
import org.example.carshering.dto.request.ChangePasswordRequest;
import org.example.carshering.identity.api.dto.response.ResetPasswordResponse;
import org.example.carshering.identity.api.dto.request.*;
import org.example.carshering.identity.api.dto.response.VerifyStatusResponse;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.application.dto.response.DocumentDto;
import org.example.carshering.identity.application.dto.response.RoleDto;
import org.example.carshering.identity.application.event.EmailVerificationCompletedEvent;
import org.example.carshering.identity.application.event.PasswordResetCompletedEvent;
import org.example.carshering.identity.application.mapper.ClientMapperForRepo;
import org.example.carshering.identity.application.service.ClientApplicationService;
import org.example.carshering.identity.domain.model.Client;
import org.example.carshering.identity.domain.model.Document;
import org.example.carshering.identity.domain.model.DocumentTypeModel;
import org.example.carshering.identity.domain.model.RoleModel;
import org.example.carshering.identity.domain.repository.ClientDomainRepository;
import org.example.carshering.identity.domain.repository.DocumentTypeDomainRepository;
import org.example.carshering.identity.domain.repository.RoleDomainRepository;
import org.example.carshering.identity.domain.service.ClientUniquenessService;
import org.example.carshering.identity.domain.valueobject.document.DocumentId;
import org.example.carshering.identity.domain.valueobject.document.DocumentNumber;
import org.example.carshering.identity.domain.valueobject.document.DocumentSeries;
import org.example.carshering.identity.domain.valueobject.document.DocumentTypeId;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.domain.valueobject.role.RoleName;
import org.example.carshering.identity.domain.valueobject.user.Email;
import org.example.carshering.identity.domain.valueobject.user.Login;
import org.example.carshering.identity.domain.valueobject.user.Password;
import org.example.carshering.identity.domain.valueobject.user.Phone;
import org.example.carshering.identity.infrastructure.email.EmailService;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientApplicationServiceImpl implements ClientApplicationService {

    private final ClientDomainRepository clientRepository;
    private final ClientMapperForRepo clientMapper;
    private final EmailService emailService;
    private final DocumentTypeDomainRepository documentTypeRepository;
    private final RoleDomainRepository roleRepository;
    private static final Long DEFAULT_CLIENT_ROLE_ID = 1L;
    private final ClientUniquenessService clientUniquenessService;

    @Override
    public ClientDto createUser(RegistrationRequest createUserRequest) {

        Login login = Login.of(createUserRequest.login());
        Email email = Email.of(createUserRequest.email());

        clientUniquenessService.ensureUnique(email, login);



        Password password = Password.ofEncoded(createUserRequest.password());
        Client client = (Client.create(
                null,
                createUserRequest.lastName(),
                login,
                email,
                password,
                null
        ));

        RoleModel defaultRole = roleRepository.findById(new RoleId(DEFAULT_CLIENT_ROLE_ID))
                .orElseThrow(() -> new BusinessException("Default role not found"));

        client.changeRole(defaultRole.getRoleId());
        Client savedClient = clientRepository.save(client);
        return clientMapper.toDto(savedClient);
    }

    @Override
    public ClientDto findUser(Long userId) {
        return clientMapper.toDto(clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("User not found")
        ));
    }

    @Override
    public Page<ClientDto> filterUsers(FilterUserRequest filter, Pageable pageable) {
        Page<Client> clients = clientRepository.findByFilter(filter, pageable);

        return clients.map(clientMapper::toDto);
    }

    @Override
    public void deleteUser(Long userId) {
        Client client = clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("User not found")
        );
        client.markAsDeleted();
        clientRepository.save(client);
    }

    @Override
    public ClientDto banUser(Long userId) {
        Client client = clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("User not found")
        );
        client.ban();

        return clientMapper.toDto(clientRepository.save(client));
    }

    @Override
    public ClientDto unbanUser(Long userId) {
        Client client = clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        client.unban();
        return clientMapper.toDto(clientRepository.save(client));
    }

    @Override
    public VerifyStatusResponse sendForVerifyEmail(Long userId) {
        Client client = clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        if (client.isEmailVerified()) {
            throw new BusinessException("Email already verified");
        }
        emailService.sendVerificationEmail(client);

        return new VerifyStatusResponse("VERIFICATION_EMAIL_SENT", "Verification email sent to " + client.getEmail());
    }

    @Override
    public ClientDto verifyEmail(ClientId clientId) {
        Client client = clientRepository.findById(clientId).orElseThrow(
                () -> new NotFoundException("User not found")
        );
        client.verifyEmail();


        return clientMapper.toDto(clientRepository.save(client));
    }

    @Transactional
    @Override
    public void resetPassword(String code, NewPasswordRequest request) {
        emailService.resetPassword(code, request); // TODO: use a service method for this
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        Client client = clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        Phone phone = request.phone() != null ? Phone.of(request.phone()) : client.getPhone();

        clientUniquenessService.ensureUnique(client.getClientId().value(),phone);

        String firstName = request.firstName() != null ? request.firstName() : client.getFirstName();
        String lastName = request.lastName() != null ? request.lastName() : client.getLastName();

        client.updatePersonalInfo(firstName, lastName);

        client.updatePhone(phone);

        clientRepository.save(client);
    }

    @Override
    public ClientDto updateRole(Long userId, Long roleId) {
        Client client = clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        RoleModel newRole = roleRepository.findById(new RoleId(roleId))
                .orElseThrow(() -> new BusinessException("role not found " + roleId));

        client.changeRole(newRole.getRoleId());


        return clientMapper.toDto(clientRepository.save(client));
    }

    @Override
    public ClientDto updateRoleByName(Long userId, String roleName) {
        Client client = clientRepository.findById(new ClientId(userId))
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Ищем роль по имени через Domain Repository
        RoleModel newRole = roleRepository.findByName(RoleName.of(roleName))
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));

        client.changeRole(newRole.getRoleId());
        return clientMapper.toDto(clientRepository.save(client));
    }

    @Override
    public Page<ClientDto> filterUsersByRoleName(Boolean banned, String roleName, Pageable pageable) {
        RoleId roleId = null;

        if (roleName != null) {
            RoleModel role = roleRepository.findByName(RoleName.of(roleName))
                    .orElseThrow(() -> new BusinessException("Role not found: " + roleName));
            roleId = role.getRoleId();
        }

        var filter = new FilterUserRequest(banned, roleId != null ? roleId.value() : null);
        return clientRepository.findByFilter(filter, pageable).map(clientMapper::toDto);
    }

    @Override
    public ResetPasswordResponse resetPasswordForEmail(ResetPasswordRequest request) {
        Client client = clientRepository.findByEmail(Email.of(request.email())).orElseThrow(
                () -> new NotFoundException("Client not found with id %s", request.email())
        );

        if (!client.isEmailVerified()) {
            emailService.sendVerificationEmail(client);
            throw new EmailNotVerifiedException("We can't support for help this account, you need to verified email. We have sent a new email for verification");
        }

        emailService.sendResetPasswordEmail(client);
        return new ResetPasswordResponse("RESET_EMAIL_SENT", "Reset password email sent to " + client.getEmail());
    }

    @Override
    @Transactional
    public void updatePasswordForEmail(ClientId clientId, String password) {
        Client client = clientRepository.findById(clientId).orElseThrow(
                () -> new NotFoundException("Client not found with id %s", String.valueOf(clientId.value()))
        );

        client.changePassword(client.getPassword().getValue(), Password.ofEncoded(password));
        clientRepository.save(client);
        emailService.sendPasswordResetConfirmationEmail(client);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        Client client = clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("Client not found with id %s", String.valueOf(userId))
        );

        client.changePassword(request.oldPassword(), Password.ofEncoded(request.newPassword()));
        clientRepository.save(client);

    }

    @Override
    public DocumentDto createDocument(CreateDocumentRequest request, Long userId) {
        log.info("Creating document for user ID {}: {}", userId, request);

        Client client = clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        if (client.getActiveDocument() != null) {
            throw new BusinessException("Client already has an active document. Delete it first.");
        }
        log.info("Creating document for user ID {}: {}", userId, request);

        DocumentTypeModel docType = documentTypeRepository.findById(new DocumentTypeId(request.documentTypeId()))
                .orElseThrow(() -> new NotFoundException("Document type not found"));

        log.info("Document type found: {}", docType);

        log.info("Creating document for user ID {}: {}", userId, request);

        DocumentSeries series = DocumentSeries.of(request.series());

        log.info("Document series set to: {}", series);

        DocumentNumber number = DocumentNumber.of(request.number());

        log.info("Document number set to: {}", number);

        DateOfIssue dateOfIssue = DateOfIssue.of(request.dateOfIssue());

        log.info("Document date of issue set to: {}", dateOfIssue);

        IssuingAuthority authority = IssuingAuthority.of(request.issuingAuthority());

        log.info("Document issuing authority set to: {}", authority);


        client.addDocument(new DocumentTypeId(request.documentTypeId()), series, number, dateOfIssue, authority);

        log.info("он зашел?");
        log.info("Adding document for client ID {}: {}, document = {} ", userId, request, client.getActiveDocument() == null ? "null" : client.getActiveDocument().toString());

        Client savedClient = clientRepository.save(client);

        log.info("Document added to client ID {}: {}", userId, savedClient.getActiveDocument());

        Document createdDocument = savedClient.getActiveDocument();
        if (createdDocument == null) {
            throw new BusinessException("Failed to create document");
        }

        log.info("Document created successfully for user ID {}: {}", userId, createdDocument);

        return clientMapper.toDto(createdDocument);
    }

    @Override
    public boolean hasDocument(Long userId) {
        return clientRepository.findById(
                new ClientId(userId)
        ).orElseThrow(
                () -> new NotFoundException("User not found")
        ).getActiveDocument() != null;
    }

    @Override
    public DocumentDto findDocument(Long userId) {
        return clientMapper.toDto(clientRepository.findById(
                new ClientId(userId)
        ).orElseThrow(
                () -> new NotFoundException("User not found")
        ).getActiveDocument());
    }

    @Override
    public DocumentDto findValidDocument(Long userId) {
        return clientMapper.toDto(clientRepository.findById(
                new ClientId(userId)
        ).orElseThrow(
                () -> new NotFoundException("User not found")
        ).getActiveAndValidDocument());
    }


    @Override
    public DocumentDto updateDocument(Long userId, UpdateDocumentRequest request) {
        Client client = clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        log.info("Updating document for client ID {}: {}", userId, request);

        if (client.getActiveDocument() == null) {
            throw new BusinessException("Client has no document to update");
        }

        log.info("active document found: {}, client has document: {}", client.getActiveDocument(), client.getActiveDocument() == null);

        DocumentId oldDocId = client.getActiveDocument().getDocumentId();


        DocumentTypeId documentTypeId = client.getActiveDocument().getDocumentType();

        log.info("Current document type ID: {}", documentTypeId);

        if (request.documentTypeId() != null) {
            documentTypeRepository.findById(new DocumentTypeId(request.documentTypeId())).orElseThrow(
                    () -> new NotFoundException("Document type not found")
            );
            documentTypeId = new DocumentTypeId(request.documentTypeId());

        }

        DocumentSeries series = request.series() != null
                ? DocumentSeries.of(request.series())
                : client.getActiveDocument().getDocumentSeries();
        log.info("Series set to: {}", series);

        DocumentNumber number = request.number() != null
                ? DocumentNumber.of(request.number())
                : client.getActiveDocument().getDocumentNumber();
        log.info("Number set to: {}", number);

        DateOfIssue dateOfIssue = request.dateOfIssue() != null
                ? DateOfIssue.of(request.dateOfIssue())
                : client.getActiveDocument().getDateOfIssue();

        log.info("Date set to: {}", dateOfIssue);

        IssuingAuthority authority = request.issuingAuthority() != null
                ? IssuingAuthority.of(request.issuingAuthority())
                : client.getActiveDocument().getIssuingAuthority();
        log.info("Issuing authority set to: {}", authority);

        client.removeDocument(oldDocId);
        log.info("Removing document for client ID {}: {}", userId, request);
        client.addDocument(documentTypeId, series, number, dateOfIssue, authority);

        log.info("Adding document for client ID {}: {}, document = {} ", userId, request, client.getActiveDocument());

        Client savedClient = clientRepository.save(client);

        return clientMapper.toDto(savedClient.getActiveDocument());
    }

    @Override
    public void verifyDocument(Long documentId) {
        Client client = clientRepository.findByDocumentId(new DocumentId(documentId)).orElseThrow(
                () -> new NotFoundException("Document not found")
        );
        client.verifyDocument(new DocumentId(documentId));

    }

    @Override
    public Page<DocumentDto> getAllDocuments(boolean onlyUnverified, Pageable pageable) {
        return clientRepository.getAllDocuments(onlyUnverified, pageable).map(clientMapper::toDto);
    }

    @Override
    public void deleteDocument(Long userId) {
        Client client = clientRepository.findById(new ClientId(userId)).orElseThrow(
                () -> new NotFoundException("User not found")
        );
        client.removeDocument(client.getActiveDocument().getDocumentId());
        clientRepository.save(client);
    }

    @Override
    public Optional<ClientDto> findUserByLogin(String username) {
        Optional<Client> client = clientRepository.findByLogin(Login.of(username));
        return client.map(clientMapper::toDto);
    }

    @Override
    public Optional<RoleDto> findRoleByRoleId(Long aLong) {
        return  roleRepository.findById(new RoleId(aLong)).map( clientMapper::toDto);
    }

    // ============== Event Handlers ==============

    /**
     * Обработчик события завершения верификации email
     * Вызывается когда пользователь успешно подтвердил email через ссылку
     */
    @EventListener
    @Transactional
    public void handleEmailVerificationCompleted(EmailVerificationCompletedEvent event) {
        verifyEmail(event.clientId());
    }

    /**
     * Обработчик события завершения сброса пароля
     * Вызывается когда пользователь успешно сбросил пароль через email
     */
    @EventListener
    @Transactional
    public void handlePasswordResetCompleted(PasswordResetCompletedEvent event) {
        updatePasswordForEmail(event.clientId(), event.newPassword());
    }
}
