package org.example.carshering.identity.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.example.carshering.common.exceptions.custom.BusinessException;
import org.example.carshering.identity.domain.service.ClientUniquenessService;
import org.example.carshering.identity.domain.valueobject.user.Email;
import org.example.carshering.identity.domain.valueobject.user.Login;
import org.example.carshering.identity.domain.valueobject.user.Phone;
import org.example.carshering.identity.infrastructure.persistence.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientUniquenessServiceImpl implements ClientUniquenessService {
    private final ClientRepository clientRepository;

    @Override
    public void ensureUnique(Email email, Login login) {
        if (clientRepository.existsByEmail(email.getValue())) {
            throw new BusinessException("Email already exists");
        }
        if (clientRepository.existsByLoginAndDeletedFalse(login.getValue())) {
            throw new BusinessException("Login already exists");
        }
    }

    @Override
    public void ensureUnique(Long id, Phone phone) {
        if (clientRepository.existsByPhoneAndDeletedFalse(id,phone.getValue())) {
            throw new BusinessException("Phone number already exists");
        }
    }
}
