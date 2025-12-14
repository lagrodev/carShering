package org.example.carshering.identity.application.event;

import org.example.carshering.common.domain.valueobject.ClientId;

/**
 * Событие завершения сброса пароля
 * Публикуется когда пользователь успешно сбросил пароль через email
 */
public record PasswordResetCompletedEvent(ClientId clientId, String newPassword) {
}

