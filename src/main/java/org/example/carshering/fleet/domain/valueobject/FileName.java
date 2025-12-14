package org.example.carshering.fleet.domain.valueobject;

import java.util.regex.Pattern;

public record FileName(
        String value
) {
    private static final Pattern VALID_EXTENSION = Pattern.compile(".*\\.(jpg|jpeg|png|webp|gif)$", Pattern.CASE_INSENSITIVE);
    private static final int MAX_LENGTH = 255;

    public FileName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FileName cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("FileName too long (max " + MAX_LENGTH + " characters)");
        }
        if (!VALID_EXTENSION.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid file extension. Allowed: jpg, jpeg, png, webp, gif");
        }
    }

    public String getExtension() {
        int lastDotIndex = value.lastIndexOf('.');
        return lastDotIndex > 0 ? value.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    public String getNameWithoutExtension() {
        int lastDotIndex = value.lastIndexOf('.');
        return lastDotIndex > 0 ? value.substring(0, lastDotIndex) : value;
    }
}
