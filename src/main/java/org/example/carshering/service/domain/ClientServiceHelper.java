//package org.example.carshering.service.domain;
//
//import jakarta.validation.ValidationException;
//import lombok.RequiredArgsConstructor;
//import org.example.carshering.identity.infrastructure.persistence.entity.Client;
//import org.example.carshering.identity.infrastructure.persistence.repository.ClientRepository;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class ClientServiceHelper {
//
//    private final ClientRepository clientRepository;
//
//
//    public Client getEntity(Long userId) {
//        return clientRepository.findById(userId)
//                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
//    }
//}
