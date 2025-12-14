//package org.example.carshering.service.domain;
//
//import lombok.RequiredArgsConstructor;
//import org.example.carshering.identity.infrastructure.persistence.entity.Client;
//import org.example.carshering.common.exceptions.custom.BusinessConflictException;
//import org.example.carshering.rental.infrastructure.persistence.entity.ContractJpaEntity;
//import org.example.carshering.rental.infrastructure.persistence.repository.ContractRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//public class ContractServiceHelper {
//
//    private static final Set<String> ACTIVE_STATES = Set.of("ACTIVE", "PENDING", "CANCELLATION REQUESTED", "CONFIRMED");
//
//    private final ContractRepository contractRepository;
//
//    public void checkAndAllActiveContractsByClient(Client client) {
//        List<ContractJpaEntity> activeContracts = contractRepository.findAllByClientAndActiveStates(client, ACTIVE_STATES);
//        if (!activeContracts.isEmpty()) {
//            ContractJpaEntity c = activeContracts.getFirst();
//            throw new BusinessConflictException("Active contract exists: ID " + c.getId() + ", state: " + c.getState().name());
//        }
//
//    }
//}
