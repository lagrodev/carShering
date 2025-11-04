package org.example.carshering.service.domain;

import org.example.carshering.entity.Car;
import org.example.carshering.entity.Contract;
import org.example.carshering.repository.ContractRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RentalDomainServiceTests {

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private RentalDomainService serviceUnderTest;

    @Test
    @DisplayName("Test isCarAvailable returns true when no overlapping contracts found")
    public void givenNoOverlappingContracts_whenIsCarAvailable_thenReturnTrue() {
        // given
        LocalDate start = LocalDate.of(2025, 1, 10);
        LocalDate end = LocalDate.of(2025, 1, 15);
        Long carId = 1L;

        given(contractRepository.findOverlappingContracts(start, end, carId))
                .willReturn(List.of());

        // when
        boolean result = serviceUnderTest.isCarAvailable(start, end, carId);

        // then
        assertThat(result).isTrue();
        verify(contractRepository).findOverlappingContracts(start, end, carId);
    }

    @Test
    @DisplayName("Test isCarAvailable returns false when overlapping contracts exist")
    public void givenOverlappingContracts_whenIsCarAvailable_thenReturnFalse() {
        // given
        LocalDate start = LocalDate.of(2025, 1, 10);
        LocalDate end = LocalDate.of(2025, 1, 15);
        Long carId = 1L;

        Contract overlappingContract = new Contract();
        given(contractRepository.findOverlappingContracts(start, end, carId))
                .willReturn(List.of(overlappingContract));

        // when
        boolean result = serviceUnderTest.isCarAvailable(start, end, carId);

        // then
        assertThat(result).isFalse();
        verify(contractRepository).findOverlappingContracts(start, end, carId);
    }

    @Test
    @DisplayName("Test calculateCost returns correct total for multiple days")
    public void givenValidDates_whenCalculateCost_thenReturnCorrectTotal() {
        // given
        Car car = new Car();
        car.setRent(100.0);

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 6); // 5 days difference

        // when
        double total = serviceUnderTest.calculateCost(car, start, end);

        // then
        assertThat(total).isEqualTo(500.0);
    }

    @Test
    @DisplayName("Test calculateCost returns zero when start and end dates are the same")
    public void givenSameStartAndEndDate_whenCalculateCost_thenReturnZero() {
        // given
        Car car = new Car();
        car.setRent(100.0);

        LocalDate date = LocalDate.of(2025, 1, 1);

        // when
        double total = serviceUnderTest.calculateCost(car, date, date);

        // then
        assertThat(total).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Test calculateCost handles reversed date order correctly (negative days)")
    public void givenEndBeforeStart_whenCalculateCost_thenReturnNegativeCost() {
        // given
        Car car = new Car();
        car.setRent(150.0);

        LocalDate start = LocalDate.of(2025, 1, 10);
        LocalDate end = LocalDate.of(2025, 1, 5); // end before start

        // when
        double total = serviceUnderTest.calculateCost(car, start, end);

        // then
        assertThat(total).isEqualTo(-750.0); // -5 days * 150
    }

    @Test
    @DisplayName("Test calculateCost with fractional rent value")
    public void givenCarWithFractionalRent_whenCalculateCost_thenReturnAccurateTotal() {
        // given
        Car car = new Car();
        car.setRent(99.99);

        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 3, 4); // 3 days

        // when
        double total = serviceUnderTest.calculateCost(car, start, end);

        // then
        assertThat(total).isCloseTo(299.97, within(0.001));
    }


}
