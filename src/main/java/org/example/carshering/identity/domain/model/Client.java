package org.example.carshering.identity.domain.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.common.exceptions.custom.BusinessException;
import org.example.carshering.common.exceptions.custom.InvalidPasswordException;
import org.example.carshering.fleet.domain.valueobject.DateOfIssue;
import org.example.carshering.fleet.domain.valueobject.IssuingAuthority;
import org.example.carshering.identity.domain.valueobject.document.DocumentId;
import org.example.carshering.identity.domain.valueobject.document.DocumentNumber;
import org.example.carshering.identity.domain.valueobject.document.DocumentSeries;
import org.example.carshering.identity.domain.valueobject.document.DocumentTypeId;
import org.example.carshering.identity.domain.valueobject.role.RoleId;
import org.example.carshering.identity.domain.valueobject.user.Email;
import org.example.carshering.identity.domain.valueobject.user.Login;
import org.example.carshering.identity.domain.valueobject.user.Password;
import org.example.carshering.identity.domain.valueobject.user.Phone;
import org.example.carshering.common.domain.valueobject.ClientId;

@Getter
@Slf4j
public class Client {
    // final fields
    private final ClientId clientId;

    // личные данные
    private String firstName;
    private String lastName;
    private Login login;
    private Email email;
    private Password password;
    private Phone phone;

    // состояние аккаунта
    private boolean deleted = false;
    private boolean banned = false;
    private boolean emailVerified = false;

    // роль пользователя
    private RoleId role = null;

    // документы пользователя
    private Document activeDocument = null;

    // Геттер для RoleId (для маппинга в JPA)
    public RoleId getRoleId() {
        return role != null ? role : null;
    }

    private Client(ClientId clientId, String firstName, String lastName,
                   Login login, Email email, Password password, Phone phone
                   ) {
        this.clientId = clientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }


    // создание нового клиента - defaultRoleId - роль по умолчанию для новых клиентов (clientRoleId)
    public static Client create(String firstName, String lastName,
                                Login login, Email email, Password password,
                                Phone phone) {
        return new Client(null, firstName, lastName, login, email, password, phone);
    }

    public static Client reconstruct(ClientId clientId, String firstName, String lastName,
                                     Login login, Email email, Password password,
                                     Phone phone,
                                     boolean deleted, boolean banned, boolean emailVerified,
                                     Document activeDocument, RoleId role) {
        Client client = new Client(clientId, firstName, lastName, login, email, password, phone);
        client.deleted = deleted;
        client.banned = banned;
        client.emailVerified = emailVerified;
        client.activeDocument = activeDocument;
        client.role = role;
        return client;
    }

    // поменять роль клиента
    public void changeRole(RoleId newRole) {
        if (banned || deleted) {
            throw new BusinessException("Cannot change role of a banned or deleted client");
        } // если клиент забанен или удален, нельзя менять роль

        this.role = newRole;
    }

    // документы - use case для добавления или обновления документа

    public void addDocument(DocumentTypeId documentType, DocumentSeries series,
                            DocumentNumber number, DateOfIssue dateOfIssue,
                            IssuingAuthority issuingAuthority) {

        log.info("Adding document {}", documentType.value());
        log.info("activeDocument is null: {}", this.activeDocument == null);

        if (this.activeDocument != null && this.activeDocument.isValid()) {
            throw new BusinessException("Active document already exists");
        }





        this.activeDocument = Document.create(documentType, series, number, dateOfIssue, issuingAuthority);
        log.info("activeDocument is null: {}", this.activeDocument == null);
        log.info("Document added with ID: {}", this.activeDocument.getDocumentId());

    }



    public void verifyDocument(DocumentId documentId) {
        if (this.activeDocument == null) {
            throw new BusinessException("No document to verify");
        }

        if (!this.activeDocument.getDocumentId().equals(documentId)) {
            throw new BusinessException("Document ID mismatch");
        }

        // Вызываем package-private метод
        activeDocument.verify();
    }

    public void removeDocument(DocumentId documentId) {
        if (this.activeDocument == null) {
            throw new BusinessException("No document to remove");
        }
        if (!this.activeDocument.getDocumentId().equals(documentId)) {
            throw new BusinessException("Document ID mismatch");
        }

        // Вызываем package-private метод
        activeDocument.markAsDelete();
    }

    public Document getActiveDocument() {

        return activeDocument != null ? activeDocument : null;
    }

    public Document getActiveAndValidDocument() {
        if (activeDocument != null && activeDocument.isValid()) {
            return activeDocument;
        }
        return null;
    }

    void setActiveDocument(Document doc) {
        this.activeDocument = doc;
    }

    public void ban() {
        if (banned) {
            throw new BusinessException("Client is already banned");
        }
        this.banned = true;
    }

    public void unban() {
        if (!banned){
            throw new BusinessException("Client is not banned");
        }
        this.banned = false;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void markAsDeleted() {
        this.deleted = true;
    }

    public void updatePersonalInfo(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new BusinessException("First name cannot be empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new BusinessException("Last name cannot be empty");
        }
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void updateEmail(Email newEmail) {
        this.email = newEmail;
        this.emailVerified = false; // Нужно заново верифицировать
    }

    public void updatePhone(Phone newPhone) {
        this.phone = newPhone;
    }

    /**
     * Change user password.
     *
     * @param oldPlainPassword   plain text old password to verify
     * @param newEncodedPassword NEW ENCODED password (must be already encoded with BCrypt)
     * @throws InvalidPasswordException if old password is incorrect
     */
    public void changePassword(String oldPlainPassword, Password newEncodedPassword) {
        if (oldPlainPassword == null || oldPlainPassword.isBlank()) {
            throw new InvalidPasswordException("Old password cannot be empty");
        }

        if (newEncodedPassword == null) {
            throw new InvalidPasswordException("New password cannot be null");
        }

        if (!this.password.matches(oldPlainPassword)) {
            throw new InvalidPasswordException("Old password is incorrect");
        }

        this.password = newEncodedPassword;
    }



}
