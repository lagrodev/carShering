package org.example.carshering.identity.application.event;

import org.example.carshering.common.domain.valueobject.ClientId;

/**
 * Событие завершения верификации email
 * Публикуется когда пользователь успешно подтвердил свой email
 */
public record EmailVerificationCompletedEvent(ClientId clientId) {
}

