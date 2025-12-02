package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.*;
import org.example.carshering.entity.Car;
import org.example.carshering.entity.CarState;
import org.example.carshering.entity.RentalState;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.repository.AnalysisRepository;
import org.example.carshering.repository.RentalStateRepository;
import org.example.carshering.service.interfaces.AnalysisService;
import org.example.carshering.service.interfaces.CarStateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    //clickhouse

    private  final AnalysisRepository analysisRepository;

    private LocalTime getAverageStartTime(Long userId) {
        List<LocalDateTime> starts = analysisRepository.findDataStartsByUserId(userId);

        if (starts.isEmpty()) {
            return null; // или LocalTime.MIDNIGHT, если нужно значение по умолчанию
        }

        long totalMinutes = starts.stream()
                .mapToLong(ldt -> ldt.getHour() * 60L + ldt.getMinute())
                .sum();

        long avgMinutes = totalMinutes / starts.size();

        int hour = (int) (avgMinutes / 60) % 24;
        int minute = (int) (avgMinutes % 60);

        return LocalTime.of(hour, minute);
    }

    @Override
    // todo когда  вы чаще всего ездите
    public UserStats getOverviewStats(Long userId) {

        Car favoriteCar = analysisRepository.findByFavoriteCar(userId)
                .or(() -> analysisRepository.findByMostUsedCar(userId))
                .orElse(null);

        String favoriteBrand = analysisRepository.findByFavoritesBrand(userId).orElse(null);
        String favoriteCarClass = analysisRepository.findByFavoritesCarClass(userId).orElse(null);

        Optional<LocalDateTime> lastRideDateTimeOpt = analysisRepository.findLastRideDateTime(userId);
        LocalDate lastRideDate = lastRideDateTimeOpt
                .map(LocalDateTime::toLocalDate)
                .orElse(null);

        Integer totalRidesOpt = analysisRepository.findByTotalRidesdes(userId);
        int totalRides = totalRidesOpt == null ? 0 : totalRidesOpt;

        Long totalSpent = Optional.ofNullable(
                analysisRepository.findByTotalSpent(userId)
        ).orElse(0L);

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Long ridesThisMonth = Optional.ofNullable(
                analysisRepository.totalMinutesThisMonth(userId, startOfMonth)
        ).orElse(0L);

        Double averageCost = Optional.ofNullable(
                analysisRepository.getAverageCostPerHour(userId)
        ).orElse(0D);

        Double averageTimeDrive = Optional.ofNullable(
                analysisRepository.getAverageTimeDrive(userId)
        ).orElse(0D);
        LocalTime averageTimeToStartDrive = getAverageStartTime(userId);

        return UserStats.builder()
                .favoriteBrand(favoriteBrand)
                .topUsedCarClass(favoriteCarClass)
                .lastRideDate(lastRideDate)
                .totalRides(totalRides)
                .ridesThisMonth(ridesThisMonth)
                .totalSpent(totalSpent)

                // если favoriteCar нашёлся
                .favoriteCarId(favoriteCar != null ? favoriteCar.getId() : null)
                .favoriteCarCarClass(favoriteCar != null ? favoriteCar.getModel().getCarClass().getName() : null)
                .favoriteCarModelName(favoriteCar != null ? favoriteCar.getModel().getModel().getName() : null)
                .favoriteCarBrand(favoriteCar != null ? favoriteCar.getModel().getBrand().getName() : null)
//                .favoriteCarImageUrl(favoriteCar != null ? favoriteCar.getImageUrl() : null)
                .averageCost(averageCost)
                .averageTimeDrive(averageTimeDrive)
                .averageTimeToStartDrive(averageTimeToStartDrive)
                .build();
    }

    private final RentalStateRepository rentalStateRepository;

    @Override
    public Page<RideStats> getRidesLastMonth(Pageable  pageable) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        return analysisRepository.findTopUsersByRidesAndDriveTime(
                startOfMonth,
                startOfNextMonth,
                pageable
        );

    }




    @Override
    public Page<RideStats> getRideAllTime(Pageable  pageable) {
        return analysisRepository.findTopUsersAllTimeByDriveTime(
                pageable
        );

    }


    @Override
    public AdminOverview getOverviewStatsAdmin() {
        Long allRidesMinute = analysisRepository.allRidesMinute();

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);



        Long allRidesMinuteLastMonth = analysisRepository.allRidesMinuteLastMonth(startOfMonth,
                startOfNextMonth);
        RentalState rentalState = rentalStateRepository.findByNameIgnoreCase("COMPLETED").orElseThrow(() -> new NotFoundException("Rental State Not Found"));
        Long allContracts = analysisRepository.getAllContractsCount(rentalState);
        Long allContractsMonth = analysisRepository.getAllContractsCountLastMonth(rentalState, startOfMonth,
                startOfNextMonth);

        Long totalUsers = analysisRepository.totalUsers();
        Long totalActiveUsers = analysisRepository.totalActiveUsers();

        Long totalCars = analysisRepository.getAllCar();

        CarState state = carStateService.getStateByName("AVAILABLE");
        Long totalAvailableCars = analysisRepository.findAllCarByState(state);
        Double profit = analysisRepository.totalRevenue();
        Double profitThisMonth = analysisRepository.profitThisMonth(startOfMonth,
                startOfNextMonth);


        return AdminOverview.builder()

                .allRidesMinute(allRidesMinute)
                .allRidesMinuteThisMonth(allRidesMinuteLastMonth)

                .allContracts(allContracts)
                .allContractsMonth(allContractsMonth)

                .totalUsers(totalUsers)
                .totalActiveUsers(totalActiveUsers)

                .totalCars(totalCars)
                .totalAvailableCars(totalAvailableCars)

                .profit(profit)
                .profitThisMonth(profitThisMonth)

                .build();
    }

    @Override
    public List<DailyRevenueResponse> getDailyRevenueBetween(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        RentalState completed = rentalStateRepository.findByNameIgnoreCase("COMPLETED")
                .orElseThrow(() -> new NotFoundException("COMPLETED state not found"));

        return analysisRepository.getDailyRevenueBetween(completed, start, end).stream()
                .map(record -> {
                    LocalDate date = record[0] instanceof LocalDate
                            ? (LocalDate) record[0]
                            : ((java.sql.Date) record[0]).toLocalDate();
                    Double revenue = record[1] != null ? ((Number) record[1]).doubleValue() : 0.0;
                    return new DailyRevenueResponse(date, revenue);
                })
                .toList();
    }

    @Override
    public List<ContractDetailResponse> getContractDetailsForDay(LocalDate date) {
        RentalState completed = rentalStateRepository.findByNameIgnoreCase("COMPLETED")
                .orElseThrow(() -> new NotFoundException("COMPLETED state not found"));

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return analysisRepository.getContractsByDay(completed, start, end);
    }

    @Override
    public Page<CarAnalyticsResponse> getTopCarsByProfit(LocalDate from, LocalDate to, Pageable pageable) {
        RentalState completed = rentalStateRepository.findByNameIgnoreCase("COMPLETED")
                .orElseThrow(() -> new NotFoundException("COMPLETED state not found"));

        LocalDateTime startDate = from.atStartOfDay();
        LocalDateTime endDate = to.plusDays(1).atStartOfDay();

        return analysisRepository.getTopCarsByProfit(completed, startDate, endDate, pageable);
    }

    @Override
    public Page<CarAnalyticsResponse> getAllCarsAnalytics(LocalDate from, LocalDate to, Pageable pageable) {
        RentalState completed = rentalStateRepository.findByNameIgnoreCase("COMPLETED")
                .orElseThrow(() -> new NotFoundException("COMPLETED state not found"));

        LocalDateTime startDate = from.atStartOfDay();
        LocalDateTime endDate = to.plusDays(1).atStartOfDay();

        return analysisRepository.getAllCarsAnalytics(completed, startDate, endDate, pageable);
    }

    private final CarStateService carStateService;
}
