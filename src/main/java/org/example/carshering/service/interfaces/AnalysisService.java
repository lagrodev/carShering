package org.example.carshering.service.interfaces;

import org.example.carshering.dto.response.ContractDetailResponse;
import org.example.carshering.dto.response.DailyRevenueResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AnalysisService {
    Object getOverviewStats(Long userId);

    Object getRideAllTime(Pageable pageable);

    Object getRidesLastMonth(Pageable pageable);

    Object getOverviewStatsAdmin();

    List<DailyRevenueResponse> getDailyRevenueBetween(LocalDate from, LocalDate to);

    List<ContractDetailResponse> getContractDetailsForDay(LocalDate date);
}
