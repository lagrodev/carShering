package org.example.carshering.repository;

import org.example.carshering.domain.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisRepository extends JpaRepository<Car, Long> {
//    @Query(
//"""
//        SELECT c from Car c
//                JOIN c.contracts contr
//                        join c.favorites fav
//                                where contr.client.id = :userId and fav.client.id = :userId
//                                        GROUP BY c.id ORDER BY SUM(contr.durationMinutes) DESC LIMIT 1
//        """)
//    Optional<Car> findByFavoriteCar(@Param("userId") Long userId);
//
//    @Query("""
//select c from Car
// c join c.contracts contr
// where  contr.client.id = :userId
// GROUP BY c.id
//  ORDER BY SUM(contr.durationMinutes) DESC
//   LIMIT 1
//""")
//    Optional<Car> findByMostUsedCar(@Param("userId") Long userId);
//
//    @Query("""
//SELECT c.model.brand.name from Car c
//join c.contracts contr
//where contr.client.id = :userId
//group by c.model.brand.name
//order by sum(contr.durationMinutes) DESC
//LIMIT 1
//""")
//    Optional<String> findByFavoritesBrand(@Param("userId") Long userId);
//
//    @Query("""
//SELECT c.model.carClass.name from Car c
//join c.contracts contr
//where contr.client.id = :userId
//group by c.model.carClass.name
//order by sum(contr.durationMinutes) DESC
//LIMIT 1
//""")
//    Optional<String> findByFavoritesCarClass(@Param("userId") Long userId);
//
//    @Query("""
//SELECT contr.dataEnd FROM Contract contr
//WHERE contr.client.id = :userId
//ORDER BY contr.dataEnd DESC
//LIMIT 1
//""")
//    Optional<LocalDateTime> findLastRideDateTime(@Param("userId") Long userId);
//
//    @Query("""
//SELECT COUNT(contr) FROM Contract contr
//WHERE contr.client.id = :userId
//""")
//    Integer findByTotalRidesdes(Long userId);
//
//    @Query("""
//select sum(contr.totalCost.amount) from Contract contr
//WHERE contr.client.id = :userId
//""")
//    Long findByTotalSpent(@Param("userId") Long userId);
//
//    @Query("""
//select avg(contr.totalCost.amount/contr.durationMinutes*60)
//from Contract contr
//where contr.client.id = :userId
//AND contr.durationMinutes > 0
//""")
//    Double getAverageCostPerHour(@Param("userId") Long userId);
//
//    @Query("""
//select sum(contr.durationMinutes) from Contract contr
//where contr.client.id = :userId
//AND contr.dataStart >= :startOfMonth
//""")
//    Long totalMinutesThisMonth(@Param("userId") Long userId, @Param("startOfMonth") LocalDateTime startOfMonth);
//
//    @Query("""
//select avg(contr.durationMinutes) from Contract contr
//where contr.client.id = :userId
//AND contr.durationMinutes > 0
//""")
//    Double getAverageTimeDrive(@Param("userId") Long userId);
//
//    @Query("SELECT c.dataStart FROM Contract c WHERE c.client.id = :userId")
//    List<LocalDateTime> findDataStartsByUserId(Long userId);
//
//
//    @Query("""
//    SELECT NEW org.example.carshering.dto.response.RideStats(
//        c.client.id,
//        c.client.login,
//        COUNT(c),
//        SUM(c.durationMinutes),
//        SUM(c.durationMinutes / 60.0)
//    )
//    FROM Contract c
//    WHERE c.durationMinutes > 0
//      AND c.dataStart >= :startOfMonth
//      AND c.dataStart < :startOfNextMonth
//    GROUP BY c.client.id, c.client.login
//    ORDER BY COUNT(c) DESC, SUM(c.durationMinutes) DESC
//""")
//    Page<RideStats> findTopUsersByRidesAndDriveTime(
//            @Param("startOfMonth") LocalDateTime startOfMonth,
//            @Param("startOfNextMonth") LocalDateTime startOfNextMonth,
//            Pageable pageable
//    );
//
//
//    @Query("""
//    SELECT NEW org.example.carshering.dto.response.RideStats(
//        c.client.id,
//         c.client.login,
//        COUNT(c),
//        SUM(c.durationMinutes),
//        SUM(c.durationMinutes / 60.0)
//    )
//    FROM Contract c
//    WHERE c.durationMinutes > 0
//    GROUP BY c.client.id, c.client.login
//    ORDER BY SUM(c.durationMinutes) DESC, COUNT(c) DESC
//""")
//    Page<RideStats> findTopUsersAllTimeByDriveTime(
//            Pageable pageable
//    );
//
//
//
//    @Query(
//            """
//     SELECT sum(contr.durationMinutes) FROM Contract contr
//"""
//    )
//    Long allRidesMinute();
//
//    @Query("""
//    SELECT sum(contr.durationMinutes) FROM Contract contr
//    WHERE contr.durationMinutes > 0
//      AND contr.dataStart >= :startOfMonth
//      AND contr.dataStart < :startOfNextMonth
//"""
//    )
//    Long allRidesMinuteLastMonth(@Param("startOfMonth") LocalDateTime startOfMonth,
//                                 @Param("startOfNextMonth") LocalDateTime startOfNextMonth);
//
//    @Query(
//            """
//    SELECT count(c) from Contract c
//    where c.state = :state
//"""
//    )
//    Long getAllContractsCount(@Param("state")RentalState rentalState);
//
//
//    @Query(
//            """
//    SELECT count(c) from Contract c
//    where c.state = :state
//    AND c.dataStart >= :startOfMonth
//      AND c.dataStart < :startOfNextMonth
//"""
//    )
//    Long getAllContractsCountLastMonth(@Param("state")RentalState rentalState,
//                                       @Param("startOfMonth") LocalDateTime startOfMonth,
//                                       @Param("startOfNextMonth") LocalDateTime startOfNextMonth);
//
//    @Query(
//            """
//        SELECT count(cl) from Client cl
//"""
//    )
//    Long totalUsers();
//
//    @Query(
//            """
//        SELECT count(cl) from Client cl
//        where cl.banned = false and cl.deleted = false
//"""
//    )
//    Long totalActiveUsers();
//
//    @Query(
//            """
//        SELECT count(car) from Car car
//"""
//    )
//    Long getAllCar();
//
//    @Query(
//            """
//        SELECT count(car) from Car car
//        where car.state = :state
//"""
//    )
//    Long findAllCarByState(@Param("state") CarState state);
//
//    @Query(
//            """
//        SELECT sum(contr.totalCost.amount) from Contract contr
//"""
//    )
//    BigDecimal totalRevenue();
//
//    @Query(
//            """
//        SELECT sum(contr.totalCost.amount) from Contract contr
//        where contr.dataStart >= :startOfMonth
//      AND contr.dataStart < :startOfNextMonth
//"""
//    )
//    BigDecimal profitThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth,
//                           @Param("startOfNextMonth") LocalDateTime startOfNextMonth);
//
//
//    @Query("""
//    SELECT DATE(contr.dataStart),
//           COALESCE(SUM(contr.totalCost.amount), 0)
//    FROM Contract contr
//    WHERE contr.dataStart >= :start AND contr.dataStart < :end and contr.state = :completedState
//    GROUP BY DATE(contr.dataStart)
//    ORDER BY DATE(contr.dataStart)
//""")
//    List<Object[]> getDailyRevenueBetween(@Param("completedState") RentalState completedState, LocalDateTime start, LocalDateTime end);
//
//
//
//    @Query(
//            """
//    SELECT NEW org.example.carshering.dto.response.ContractDetailResponse(
//        contr.client.firstName,
//        contr.client.login,
//        car.model.brand.name,
//        car.model.carClass.name,
//        car.id,
//        car.model.model.name,
//        contr.totalCost.amount,
//        contr.durationMinutes,
//        contr.dataStart,
//        contr.dataEnd
//    )
//    FROM Contract contr
//    JOIN contr.client cl
//    JOIN contr.car car
//    WHERE contr.state = :completedState
//      AND contr.dataStart >= :startOfDay
//      AND contr.dataStart < :endOfDay
//    ORDER BY contr.dataStart DESC
//"""
//    )
//    List<ContractDetailResponse> getContractsByDay(@Param("completedState") RentalState completedState,
//                                                   @Param("startOfDay") LocalDateTime startOfDay,
//                                                   @Param("endOfDay") LocalDateTime endOfDay);
//
//    // Car Analytics Queries
////        c.imageUrl,
////    @Query("""
////    SELECT NEW org.example.carshering.dto.response.CarAnalyticsResponse(
////        c.id,
////        c.gosNumber,
////        c.vin,
////        c.model.brand.name,
////        c.model.model.name,
////        c.model.carClass.name,
////        c.yearOfIssue,
////
////        c.dailyRate,
////        (SELECT COALESCE(SUM(co.durationMinutes), 0L) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState),
////        (SELECT COALESCE(AVG(co.totalCost), 0.0) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState),
////        (SELECT COUNT(DISTINCT co.client.id) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState),
////        (SELECT COALESCE(SUM(co.durationMinutes), 0L) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState AND co.dataStart >= :startDate AND co.dataStart < :endDate),
////        (SELECT COALESCE(SUM(co.totalCost), 0.0) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState AND co.dataStart >= :startDate AND co.dataStart < :endDate)
////    )
////    FROM Car c
////    ORDER BY (SELECT COALESCE(SUM(co.totalCost), 0.0) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState AND co.dataStart >= :startDate AND co.dataStart < :endDate) DESC
////""")
////    Page<CarAnalyticsResponse> getTopCarsByProfit(
////            @Param("completedState") RentalState completedState,
////            @Param("startDate") LocalDateTime startDate,
////            @Param("endDate") LocalDateTime endDate,
////            Pageable pageable
////    );
//////        c.imageUrl,
////    @Query("""
////    SELECT NEW org.example.carshering.dto.response.CarAnalyticsResponse(
////        c.id,
////        c.gosNumber,
////        c.vin,
////        c.model.brand.name,
////        c.model.model.name,
////        c.model.carClass.name,
////        c.yearOfIssue,
////
////        c.dailyRate,
////        (SELECT COALESCE(SUM(co.durationMinutes), 0L) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState),
////        (SELECT COALESCE(AVG(co.totalCost), 0.0) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState),
////        (SELECT COUNT(DISTINCT co.client.id) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState),
////        (SELECT COALESCE(SUM(co.durationMinutes), 0L) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState AND co.dataStart >= :startDate AND co.dataStart < :endDate),
////        (SELECT COALESCE(SUM(co.totalCost), 0.0) FROM Contract co WHERE co.car.id = c.id AND co.state = :completedState AND co.dataStart >= :startDate AND co.dataStart < :endDate)
////    )
////    FROM Car c
////    ORDER BY c.id
////""")
////    Page<CarAnalyticsResponse> getAllCarsAnalytics(
////            @Param("completedState") RentalState completedState,
////            @Param("startDate") LocalDateTime startDate,
////            @Param("endDate") LocalDateTime endDate,
////            Pageable pageable
////    );
}