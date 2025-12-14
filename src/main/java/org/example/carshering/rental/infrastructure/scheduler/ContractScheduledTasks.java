package org.example.carshering.rental.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.carshering.rental.application.service.ContractApplicationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContractScheduledTasks {

    private final ContractApplicationService contractApplicationService;

    @Scheduled(cron = "0 */5 * * * *")
    public void activateConfirmedContracts() {
        contractApplicationService.activateConfirmedContracts();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void completeExpiredContracts() {
        contractApplicationService.completeActiveContracts();
    }
}
