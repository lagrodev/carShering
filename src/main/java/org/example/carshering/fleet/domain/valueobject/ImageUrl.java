package org.example.carshering.fleet.domain.valueobject;

import java.net.MalformedURLException;
import java.net.URL;

public record ImageUrl(
        String value
) {
    private static final int MAX_LENGTH = 500;

    public ImageUrl {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ImageUrl cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("ImageUrl too long (max " + MAX_LENGTH + " characters)");
        }
        if (!isValidUrl(value)) {
            throw new IllegalArgumentException("Invalid URL format: " + value);
        }
    }

    private boolean isValidUrl(String value) {
        try {
            URL url = new URL(value);
            String protocol = url.getProtocol();
            return "http".equals(protocol) || "https".equals(protocol);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public String getDomain() {
        try {
            URL url = new URL(value);
            return url.getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public boolean isSecure() {
        return value.startsWith("https://");
    }
}
