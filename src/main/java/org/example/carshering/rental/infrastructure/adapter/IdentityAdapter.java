package org.example.carshering.rental.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.application.dto.response.DocumentDto;
import org.example.carshering.identity.application.service.ClientApplicationService;
import org.example.carshering.rental.application.port.IdentityPort;
import org.example.carshering.common.domain.valueobject.ClientId;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentityAdapter implements IdentityPort {
    private final ClientApplicationService clientService;

    @Override
    public boolean isClientVerified(ClientId clientId) {
        try {
            ClientDto client = clientService.findUser(clientId.value());
            DocumentDto documentDto = clientService.findValidDocument(clientId.value());

            return client.emailVerified() &&
                    documentDto != null &&
                    documentDto.verified();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isClientActive(ClientId clientId) {
        try {
            ClientDto client = clientService.findUser(clientId.value());
            return !client.banned() && !client.deleted();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getClientEmail(ClientId clientId) {
        ClientDto client = clientService.findUser(clientId.value());
        return client.email();
    }
}
