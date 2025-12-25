package org.example.carshering.legacy.service.interfaces;

import org.example.carshering.legacy.dto.response.CarAnalyticsResponse;
import org.example.carshering.legacy.dto.response.ContractDetailResponse;
import org.example.carshering.legacy.dto.response.DailyRevenueResponse;
import org.springframework.data.domain.Page;
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

    Page<CarAnalyticsResponse> getTopCarsByProfit(LocalDate from, LocalDate to, Pageable pageable);

    Page<CarAnalyticsResponse> getAllCarsAnalytics(LocalDate from, LocalDate to, Pageable pageable);
}
