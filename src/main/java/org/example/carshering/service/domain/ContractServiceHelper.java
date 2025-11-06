package org.example.carshering.service.domain;

import lombok.RequiredArgsConstructor;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Contract;
import org.example.carshering.exceptions.custom.BusinessConflictException;
import org.example.carshering.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContractServiceHelper {

    private static final Set<String> ACTIVE_STATES = Set.of("ACTIVE", "PENDING", "CANCELLATION REQUESTED", "CONFIRMED");

    private final ContractRepository contractRepository;

    public void checkAndAllActiveContractsByClient(Client client) {
        List<Contract> activeContracts = contractRepository.findAllByClientAndActiveStates(client, ACTIVE_STATES);
        if (!activeContracts.isEmpty()) {
            Contract c = activeContracts.getFirst();
            throw new BusinessConflictException("Active contract exists: ID " + c.getId() + ", state: " + c.getState().getName());
        }

    }
}
