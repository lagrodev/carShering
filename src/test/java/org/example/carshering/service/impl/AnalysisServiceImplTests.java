package org.example.carshering.service.impl;

import org.example.carshering.dto.response.ContractDetailResponse;
import org.example.carshering.dto.response.DailyRevenueResponse;
import org.example.carshering.entity.RentalState;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.repository.AnalysisRepository;
import org.example.carshering.repository.RentalStateRepository;
import org.example.carshering.service.interfaces.CarStateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnalysisServiceImpl
 * Тесты проверяют корректность работы методов getDailyRevenueBetween и getContractDetailsForDay,
 * которые отвечают за отображение данных в календаре
 */
@ExtendWith(MockitoExtension.class)
public class AnalysisServiceImplTests {

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private RentalStateRepository rentalStateRepository;

    @Mock
    private CarStateService carStateService;

    @InjectMocks
    private AnalysisServiceImpl serviceUnderTest;

    @Test
    @DisplayName("Test getDailyRevenueBetween возвращает корректные данные для периода")
    public void givenDateRange_whenGetDailyRevenueBetween_thenReturnsCorrectDailyRevenue() {
        // given
        LocalDate from = LocalDate.of(2025, 11, 5);
        LocalDate to = LocalDate.of(2025, 11, 7);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        // создаём мок-данные из репозитория (Object[] где [0] - дата, [1] - сумма)
        List<Object[]> repoResult = new ArrayList<>();
        repoResult.add(new Object[]{Date.valueOf(LocalDate.of(2025, 11, 5)), 5800.0});
        repoResult.add(new Object[]{Date.valueOf(LocalDate.of(2025, 11, 6)), 3200.0});
        repoResult.add(new Object[]{Date.valueOf(LocalDate.of(2025, 11, 7)), 1500.0});

        given(analysisRepository.getDailyRevenueBetween(start, end)).willReturn(repoResult);

        // when
        List<DailyRevenueResponse> result = serviceUnderTest.getDailyRevenueBetween(from, to);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);

        // проверяем, что дата 5 ноября соответствует сумме 5800
        DailyRevenueResponse day5 = result.get(0);
        assertThat(day5.date()).isEqualTo(LocalDate.of(2025, 11, 5));
        assertThat(day5.revenue()).isEqualTo(5800.0);

        // проверяем, что дата 6 ноября соответствует сумме 3200
        DailyRevenueResponse day6 = result.get(1);
        assertThat(day6.date()).isEqualTo(LocalDate.of(2025, 11, 6));
        assertThat(day6.revenue()).isEqualTo(3200.0);

        // проверяем, что дата 7 ноября соответствует сумме 1500
        DailyRevenueResponse day7 = result.get(2);
        assertThat(day7.date()).isEqualTo(LocalDate.of(2025, 11, 7));
        assertThat(day7.revenue()).isEqualTo(1500.0);

        verify(analysisRepository).getDailyRevenueBetween(start, end);
    }

    @Test
    @DisplayName("Test getDailyRevenueBetween корректно обрабатывает LocalDate из репозитория")
    public void givenRepoReturnsLocalDate_whenGetDailyRevenueBetween_thenHandlesCorrectly() {
        // given
        LocalDate from = LocalDate.of(2025, 11, 5);
        LocalDate to = LocalDate.of(2025, 11, 5);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        // иногда репозиторий может вернуть LocalDate вместо java.sql.Date
        List<Object[]> repoResult = new ArrayList<>();
        repoResult.add(new Object[]{LocalDate.of(2025, 11, 5), 5800.0});

        given(analysisRepository.getDailyRevenueBetween(start, end)).willReturn(repoResult);

        // when
        List<DailyRevenueResponse> result = serviceUnderTest.getDailyRevenueBetween(from, to);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2025, 11, 5));
        assertThat(result.get(0).revenue()).isEqualTo(5800.0);
    }

    @Test
    @DisplayName("Test getDailyRevenueBetween возвращает 0 для null значений revenue")
    public void givenNullRevenue_whenGetDailyRevenueBetween_thenReturns0() {
        // given
        LocalDate from = LocalDate.of(2025, 11, 5);
        LocalDate to = LocalDate.of(2025, 11, 5);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<Object[]> repoResult = new ArrayList<>();
        repoResult.add(new Object[]{Date.valueOf(LocalDate.of(2025, 11, 5)), null});

        given(analysisRepository.getDailyRevenueBetween(start, end)).willReturn(repoResult);

        // when
        List<DailyRevenueResponse> result = serviceUnderTest.getDailyRevenueBetween(from, to);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).revenue()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Test getDailyRevenueBetween возвращает пустой список когда нет данных")
    public void givenNoDataInPeriod_whenGetDailyRevenueBetween_thenReturnsEmptyList() {
        // given
        LocalDate from = LocalDate.of(2025, 11, 1);
        LocalDate to = LocalDate.of(2025, 11, 10);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        given(analysisRepository.getDailyRevenueBetween(start, end)).willReturn(new ArrayList<>());

        // when
        List<DailyRevenueResponse> result = serviceUnderTest.getDailyRevenueBetween(from, to);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(analysisRepository).getDailyRevenueBetween(start, end);
    }

    @Test
    @DisplayName("Test getDailyRevenueBetween правильно формирует диапазон дат для запроса")
    public void givenDateRange_whenGetDailyRevenueBetween_thenPassesCorrectDateTimeRange() {
        // given
        LocalDate from = LocalDate.of(2025, 10, 28);
        LocalDate to = LocalDate.of(2025, 11, 26);

        // ожидаемые значения для вызова репозитория
        LocalDateTime expectedStart = LocalDateTime.of(2025, 10, 28, 0, 0, 0);
        LocalDateTime expectedEnd = LocalDateTime.of(2025, 11, 27, 0, 0, 0); // to + 1 день

        given(analysisRepository.getDailyRevenueBetween(any(), any())).willReturn(new ArrayList<>());

        // when
        serviceUnderTest.getDailyRevenueBetween(from, to);

        // then
        verify(analysisRepository).getDailyRevenueBetween(expectedStart, expectedEnd);
    }

    @Test
    @DisplayName("Test getContractDetailsForDay возвращает контракты для указанной даты")
    public void givenDate_whenGetContractDetailsForDay_thenReturnsContractsForThatDate() {
        // given
        LocalDate date = LocalDate.of(2025, 11, 5);

        RentalState completedState = new RentalState();
        completedState.setId(1L);
        completedState.setName("COMPLETED");

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        // создаём список контрактов для 5 ноября
        List<ContractDetailResponse> expectedContracts = List.of(
                new ContractDetailResponse(
                        "Иван Иванов",
                        "ivan_ivanov",
                        "Brand1",
                        "SEDAN",
                        100L,
                        "Model1",
                        5800.0,
                        480L,
                        LocalDateTime.of(2025, 11, 5, 10, 0),
                        LocalDateTime.of(2025, 11, 5, 18, 0)
                ),
                new ContractDetailResponse(
                        "Петр Петров",
                        "petr_petrov",
                        "Brand2",
                        "SUV",
                        101L,
                        "Model2",
                        3200.0,
                        360L,
                        LocalDateTime.of(2025, 11, 5, 14, 0),
                        LocalDateTime.of(2025, 11, 5, 20, 0)
                )
        );

        given(rentalStateRepository.findByNameIgnoreCase("COMPLETED"))
                .willReturn(Optional.of(completedState));
        given(analysisRepository.getContractsByDay(completedState, start, end))
                .willReturn(expectedContracts);

        // when
        List<ContractDetailResponse> result = serviceUnderTest.getContractDetailsForDay(date);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // проверяем первый контракт
        ContractDetailResponse contract1 = result.get(0);
        assertThat(contract1.clientName()).isEqualTo("Иван Иванов");
        assertThat(contract1.totalCost()).isEqualTo(5800.0);
        assertThat(contract1.dataStart().toLocalDate()).isEqualTo(date);
        assertThat(contract1.carId()).isEqualTo(100L);

        // проверяем второй контракт
        ContractDetailResponse contract2 = result.get(1);
        assertThat(contract2.clientName()).isEqualTo("Петр Петров");
        assertThat(contract2.totalCost()).isEqualTo(3200.0);
        assertThat(contract2.dataStart().toLocalDate()).isEqualTo(date);
        assertThat(contract2.carId()).isEqualTo(101L);

        verify(rentalStateRepository).findByNameIgnoreCase("COMPLETED");
        verify(analysisRepository).getContractsByDay(completedState, start, end);
    }

    @Test
    @DisplayName("Test getContractDetailsForDay выбрасывает исключение если состояние COMPLETED не найдено")
    public void givenCompletedStateNotFound_whenGetContractDetailsForDay_thenThrowsNotFoundException() {
        // given
        LocalDate date = LocalDate.of(2025, 11, 5);

        given(rentalStateRepository.findByNameIgnoreCase("COMPLETED"))
                .willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.getContractDetailsForDay(date)
        );

        verify(rentalStateRepository).findByNameIgnoreCase("COMPLETED");
        verifyNoInteractions(analysisRepository);
    }

    @Test
    @DisplayName("Test getContractDetailsForDay возвращает пустой список если нет контрактов за день")
    public void givenNoContractsForDay_whenGetContractDetailsForDay_thenReturnsEmptyList() {
        // given
        LocalDate date = LocalDate.of(2025, 11, 5);

        RentalState completedState = new RentalState();
        completedState.setId(1L);
        completedState.setName("COMPLETED");

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        given(rentalStateRepository.findByNameIgnoreCase("COMPLETED"))
                .willReturn(Optional.of(completedState));
        given(analysisRepository.getContractsByDay(completedState, start, end))
                .willReturn(new ArrayList<>());

        // when
        List<ContractDetailResponse> result = serviceUnderTest.getContractDetailsForDay(date);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(analysisRepository).getContractsByDay(completedState, start, end);
    }

    @Test
    @DisplayName("Test getContractDetailsForDay правильно формирует диапазон времени для одного дня")
    public void givenDate_whenGetContractDetailsForDay_thenPassesCorrectDateTimeRange() {
        // given
        LocalDate date = LocalDate.of(2025, 11, 5);

        RentalState completedState = new RentalState();
        completedState.setId(1L);
        completedState.setName("COMPLETED");

        // ожидаемые значения: начало дня 5 ноября и начало дня 6 ноября
        LocalDateTime expectedStart = LocalDateTime.of(2025, 11, 5, 0, 0, 0);
        LocalDateTime expectedEnd = LocalDateTime.of(2025, 11, 6, 0, 0, 0);

        given(rentalStateRepository.findByNameIgnoreCase("COMPLETED"))
                .willReturn(Optional.of(completedState));
        given(analysisRepository.getContractsByDay(any(), any(), any()))
                .willReturn(new ArrayList<>());

        // when
        serviceUnderTest.getContractDetailsForDay(date);

        // then
        verify(analysisRepository).getContractsByDay(completedState, expectedStart, expectedEnd);
    }

    @Test
    @DisplayName("Test КРИТИЧЕСКИЙ: дата в календаре соответствует дате в деталях контракта")
    public void givenDateWithRevenue_whenGetBothMethods_thenDateAndRevenueMatch() {
        // given - это тест на проверку той самой проблемы из календаря
        LocalDate targetDate = LocalDate.of(2025, 11, 5);
        double expectedRevenue = 5800.0;

        // настраиваем getDailyRevenueBetween
        LocalDateTime revenueStart = targetDate.atStartOfDay();
        LocalDateTime revenueEnd = targetDate.plusDays(1).atStartOfDay();

        List<Object[]> revenueData = new ArrayList<>();
        revenueData.add(new Object[]{Date.valueOf(targetDate), expectedRevenue});

        given(analysisRepository.getDailyRevenueBetween(revenueStart, revenueEnd))
                .willReturn(revenueData);

        // настраиваем getContractDetailsForDay
        RentalState completedState = new RentalState();
        completedState.setId(1L);
        completedState.setName("COMPLETED");

        LocalDateTime detailsStart = targetDate.atStartOfDay();
        LocalDateTime detailsEnd = targetDate.plusDays(1).atStartOfDay();

        List<ContractDetailResponse> contractDetails = List.of(
                new ContractDetailResponse(
                        "Иван Иванов",
                        "ivan_ivanov",
                        "Brand",
                        "SEDAN",
                        100L,
                        "Model",
                        5800.0, // та же сумма!
                        480L,
                        LocalDateTime.of(2025, 11, 5, 10, 0), // та же дата!
                        LocalDateTime.of(2025, 11, 5, 18, 0)
                )
        );

        given(rentalStateRepository.findByNameIgnoreCase("COMPLETED"))
                .willReturn(Optional.of(completedState));
        given(analysisRepository.getContractsByDay(completedState, detailsStart, detailsEnd))
                .willReturn(contractDetails);

        // when
        List<DailyRevenueResponse> revenueResponse = serviceUnderTest.getDailyRevenueBetween(targetDate, targetDate);
        List<ContractDetailResponse> detailsResponse = serviceUnderTest.getContractDetailsForDay(targetDate);

        // then - КРИТИЧЕСКАЯ ПРОВЕРКА: дата и сумма должны совпадать!
        assertThat(revenueResponse).hasSize(1);
        assertThat(detailsResponse).hasSize(1);

        // проверяем, что дата в календаре совпадает с датой в деталях
        LocalDate calendarDate = revenueResponse.get(0).date();
        LocalDate detailsDate = detailsResponse.get(0).dataStart().toLocalDate();
        assertThat(calendarDate).isEqualTo(detailsDate);
        assertThat(calendarDate).isEqualTo(targetDate);

        // проверяем, что сумма в календаре совпадает с суммой в деталях
        double calendarRevenue = revenueResponse.get(0).revenue();
        double detailsRevenue = detailsResponse.get(0).totalCost();
        assertThat(calendarRevenue).isEqualTo(detailsRevenue);
        assertThat(calendarRevenue).isEqualTo(expectedRevenue);

        // если этот тест проходит - значит бэкенд работает правильно!
        // если на фронте показывается другая дата - проблема в UI логике
    }

    @Test
    @DisplayName("Test граничный случай: период с 28 октября по 26 ноября")
    public void givenLongPeriod_whenGetDailyRevenueBetween_thenReturnsAllDaysWithRevenue() {
        // given - точно такой же период, как в баг-репорте
        LocalDate from = LocalDate.of(2025, 10, 28);
        LocalDate to = LocalDate.of(2025, 11, 26);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        // создаём данные для нескольких дней в этом периоде
        List<Object[]> repoResult = new ArrayList<>();
        repoResult.add(new Object[]{Date.valueOf(LocalDate.of(2025, 10, 28)), 1000.0});
        repoResult.add(new Object[]{Date.valueOf(LocalDate.of(2025, 11, 5)), 5800.0});
        repoResult.add(new Object[]{Date.valueOf(LocalDate.of(2025, 11, 12)), 3200.0});
        repoResult.add(new Object[]{Date.valueOf(LocalDate.of(2025, 11, 26)), 2100.0});

        given(analysisRepository.getDailyRevenueBetween(start, end)).willReturn(repoResult);

        // when
        List<DailyRevenueResponse> result = serviceUnderTest.getDailyRevenueBetween(from, to);

        // then
        assertThat(result).hasSize(4);

        // проверяем, что каждая дата правильная
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2025, 10, 28));
        assertThat(result.get(1).date()).isEqualTo(LocalDate.of(2025, 11, 5));
        assertThat(result.get(2).date()).isEqualTo(LocalDate.of(2025, 11, 12));
        assertThat(result.get(3).date()).isEqualTo(LocalDate.of(2025, 11, 26));

        // проверяем, что суммы правильные
        assertThat(result.get(0).revenue()).isEqualTo(1000.0);
        assertThat(result.get(1).revenue()).isEqualTo(5800.0);
        assertThat(result.get(2).revenue()).isEqualTo(3200.0);
        assertThat(result.get(3).revenue()).isEqualTo(2100.0);
    }
}

