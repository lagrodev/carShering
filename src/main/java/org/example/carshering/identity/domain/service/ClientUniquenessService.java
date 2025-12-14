package org.example.carshering.identity.domain.service;

import org.example.carshering.identity.domain.valueobject.user.Email;
import org.example.carshering.identity.domain.valueobject.user.Login;
import org.example.carshering.identity.domain.valueobject.user.Phone;

public interface ClientUniquenessService {
    void ensureUnique(Email email, Login login);

    void ensureUnique(Long id, Phone phone);
}
