package org.example.carshering.dto.request;

import java.util.List;

// CarFilterRequest.java
public record CarFilterRequest(
        List<String> brands,
        List<String> models,
        Integer minYear,
        Integer maxYear,
        String bodyType,
        List<String> carClasses,
        List<String> carState
) {
}
