package org.example.carshering.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Top user statistics for leaderboard")

@Builder
public record RideStats(
        @Schema(description = "User ID", example = "42")
        Long userId,

        @Schema(description = "User's full name", example = "petr102")
        String login,

        @Schema(description = "Number of rides in the period", example = "12")
        long ridesCount,

        @Schema(description = "Total drive time in minutes", example = "480")
        long totalMinutes,

        @Schema(description = "Total drive time in hours (rounded)", example = "8")
        double totalHours
) {
}
