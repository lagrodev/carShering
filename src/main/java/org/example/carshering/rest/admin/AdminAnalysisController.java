package org.example.carshering.rest.admin;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.ContractDetailResponse;
import org.example.carshering.dto.response.DailyRevenueResponse;
import org.example.carshering.service.interfaces.AnalysisService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RequiredArgsConstructor
@RequestMapping("/api/admin/stats")
public class AdminAnalysisController {
    // Статистические эндпоинты будут добавлены здесь
    /**
     * TODO:
     * 1. Эндпоинт для получения общей статистики по пользователям (количество зарегистрированных пользователей, активные пользователи и т.д.)
     * 2. Эндпоинт для получения статистики по арендам (количество аренд, средняя продолжительность аренды, популярные автомобили и т.д.)
     * 3. Эндпоинт для получения финансовой статистики (общий доход, средний доход на пользователя и т.д.)
     * 4. Возможность фильтрации статистики по временным периодам (день, неделя, месяц, год)
     * 5. Документация Swagger для всех эндпоинтов статистики
     * GET /api/stats/overview
     * GET /api/stats/rides?from=2025-11-01&to=2025-11-25
     * GET /api/stats/revenue?period=month
     * GET /api/stats/fleet/usage
     * GET /api/stats/users/active
     * GET /api/stats/cars/popular
     *
     */
    private final AnalysisService analysisService;

    @GetMapping("/overview")
    public ResponseEntity<?> getOverviewStats(

    ) {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getOverviewStatsAdmin());
    }

    @GetMapping("/daily-revenue")
    public  ResponseEntity<List<DailyRevenueResponse>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate to
    ){
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getDailyRevenueBetween(from, to));
    }

    @GetMapping("/contracts-by-day")
    public ResponseEntity<List<ContractDetailResponse>> getContractsByDay(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate date
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(analysisService.getContractDetailsForDay(date));
    }

}
