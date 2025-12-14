package org.example.carshering.rental.application.port;

import org.example.carshering.common.domain.valueobject.ClientId;

public interface IdentityPort {
    boolean isClientVerified(ClientId clientId);
    boolean isClientActive(ClientId clientId);
    String getClientEmail(ClientId clientId);
}
