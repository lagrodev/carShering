package org.example.carshering.dto.response;

public record CarListItemResponse(
        Long id,
        String brand,
        String carClass,     // думаю, по нему делать фильтр... в голове пиздатая идея, но пиздец, по идеи, сложно @_@
        String model,
        Integer yearOfIssue,
        Double rent,
        String status
) {}
