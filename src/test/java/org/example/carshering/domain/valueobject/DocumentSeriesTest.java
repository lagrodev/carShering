package org.example.carshering.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for DocumentSeries Value Object.
 */
class DocumentSeriesTest {

    @Test
    void shouldCreateValidDocumentSeries() {
        // Given
        String series = "1234";

        // When
        DocumentSeries documentSeries = DocumentSeries.of(series);

        // Then
        assertThat(documentSeries.getValue()).isEqualTo("1234");
    }

    @Test
    void shouldTrimWhitespace() {
        // Given
        String series = "  1234  ";

        // When
        DocumentSeries documentSeries = DocumentSeries.of(series);

        // Then
        assertThat(documentSeries.getValue()).isEqualTo("1234");
    }

    @Test
    void shouldThrowExceptionWhenSeriesIsNull() {
        // When & Then
        assertThatThrownBy(() -> DocumentSeries.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Document series cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenSeriesIsBlank() {
        // When & Then
        assertThatThrownBy(() -> DocumentSeries.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Document series cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenSeriesIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> DocumentSeries.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Document series cannot be null or blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12", "1"})
    void shouldThrowExceptionWhenSeriesTooShort(String shortSeries) {
        // When & Then
        assertThatThrownBy(() -> DocumentSeries.of(shortSeries))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid document series format")
                .hasMessageContaining("Expected 4 digits");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "123456"})
    void shouldThrowExceptionWhenSeriesTooLong(String longSeries) {
        // When & Then
        assertThatThrownBy(() -> DocumentSeries.of(longSeries))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid document series format")
                .hasMessageContaining("Expected 4 digits");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12A4", "AB12", "123X", "ABCD"})
    void shouldThrowExceptionWhenSeriesContainsLetters(String invalidSeries) {
        // When & Then
        assertThatThrownBy(() -> DocumentSeries.of(invalidSeries))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid document series format")
                .hasMessageContaining("Expected 4 digits");
    }

    @Test
    void shouldThrowExceptionWhenSeriesContainsSpecialCharacters() {
        // Given
        String series = "12-4";

        // When & Then
        assertThatThrownBy(() -> DocumentSeries.of(series))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid document series format");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "5678", "9012", "0000", "9999"})
    void shouldAcceptValidSeries(String validSeries) {
        // When
        DocumentSeries series = DocumentSeries.of(validSeries);

        // Then
        assertThat(series.getValue()).isEqualTo(validSeries);
    }

    @Test
    void shouldBeEqualWhenSeriesAreSame() {
        // Given
        DocumentSeries series1 = DocumentSeries.of("1234");
        DocumentSeries series2 = DocumentSeries.of(" 1234 ");

        // Then
        assertThat(series1).isEqualTo(series2);
        assertThat(series1.hashCode()).isEqualTo(series2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenSeriesDiffer() {
        // Given
        DocumentSeries series1 = DocumentSeries.of("1234");
        DocumentSeries series2 = DocumentSeries.of("5678");

        // Then
        assertThat(series1).isNotEqualTo(series2);
    }

    @Test
    void shouldReturnValueInToString() {
        // Given
        DocumentSeries series = DocumentSeries.of("1234");

        // When
        String result = series.toString();

        // Then
        assertThat(result).isEqualTo("1234");
    }
}

