package org.example.carshering.dto.response;

import java.time.LocalDate;

public record DailyRevenueResponse(
        LocalDate date,
        Double revenue
) {}