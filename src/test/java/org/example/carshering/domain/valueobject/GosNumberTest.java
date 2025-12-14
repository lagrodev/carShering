package org.example.carshering.domain.valueobject;

import org.example.carshering.fleet.domain.valueobject.GosNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for GosNumber Value Object.
 */
class GosNumberTest {

    @Test
    void shouldCreateValidGosNumber() {
        // Given
        String number = "А123ВС77";

        // When
        GosNumber gosNumber = GosNumber.of(number);

        // Then
        assertThat(gosNumber.getValue()).isEqualTo("А123ВС77");
    }

    @Test
    void shouldNormalizeGosNumberToUpperCase() {
        // Given
        String number = "а123вс77";

        // When
        GosNumber gosNumber = GosNumber.of(number);

        // Then
        assertThat(gosNumber.getValue()).isEqualTo("А123ВС77");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "А 123 ВС 77",
        "А-123-ВС-77",
        "А 123-ВС 77",
        " А123ВС77 "
    })
    void shouldNormalizeGosNumberByRemovingSpacesAndDashes(String input) {
        // When
        GosNumber gosNumber = GosNumber.of(input);

        // Then
        assertThat(gosNumber.getValue()).isEqualTo("А123ВС77");
    }

    @Test
    void shouldAccept9DigitRegionCode() {
        // Given
        String number = "А123ВС777";

        // When
        GosNumber gosNumber = GosNumber.of(number);

        // Then
        assertThat(gosNumber.getValue()).isEqualTo("А123ВС777");
        assertThat(gosNumber.getValue()).hasSize(9);
    }

    @Test
    void shouldThrowExceptionWhenGosNumberIsNull() {
        // When & Then
        assertThatThrownBy(() -> GosNumber.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenGosNumberIsBlank() {
        // When & Then
        assertThatThrownBy(() -> GosNumber.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenGosNumberIsTooShort() {
        // Given
        String number = "А123ВС7"; // 7 characters

        // When & Then
        assertThatThrownBy(() -> GosNumber.of(number))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid length");
    }

    @Test
    void shouldThrowExceptionWhenGosNumberIsTooLong() {
        // Given
        String number = "А123ВС7777"; // 10 characters

        // When & Then
        assertThatThrownBy(() -> GosNumber.of(number))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid length");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Ж123КЛ77",  // Invalid letter Ж
        "А123ГД77",  // Invalid letter Г
        "А123ВЦ77",  // Invalid letter Ц
        "123АВС77",  // Starts with digit
        "ААВС12377"  // Wrong format
    })
    void shouldThrowExceptionForInvalidFormat(String invalidNumber) {
        // When & Then
        assertThatThrownBy(() -> GosNumber.of(invalidNumber))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid format");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "А123ВС77",
        "В456ЕК01",
        "М789НО99",
        "Т012РС123"
    })
    void shouldAcceptValidRussianGosNumbers(String validNumber) {
        // When
        GosNumber gosNumber = GosNumber.of(validNumber);

        // Then
        assertThat(gosNumber.getValue()).isNotNull();
    }

    @Test
    void shouldExtractRegionCode() {
        // Given
        GosNumber gosNumber = GosNumber.of("А123ВС77");

        // When
        String region = gosNumber.getRegion();

        // Then
        assertThat(region).isEqualTo("77");
    }

    @Test
    void shouldExtractRegionCodeWith3Digits() {
        // Given
        GosNumber gosNumber = GosNumber.of("А123ВС777");

        // When
        String region = gosNumber.getRegion();

        // Then
        assertThat(region).isEqualTo("777");
    }

    @Test
    void shouldFormatGosNumberForDisplay() {
        // Given
        GosNumber gosNumber = GosNumber.of("А123ВС77");

        // When
        String formatted = gosNumber.getFormatted();

        // Then
        assertThat(formatted).isEqualTo("А 123 ВС 77");
    }

    @Test
    void shouldFormatGosNumberWith9CharsForDisplay() {
        // Given
        GosNumber gosNumber = GosNumber.of("А123ВС777");

        // When
        String formatted = gosNumber.getFormatted();

        // Then
        assertThat(formatted).isEqualTo("А 123 ВС 777");
    }

    @Test
    void shouldBeEqualWhenGosNumbersAreSame() {
        // Given
        GosNumber gos1 = GosNumber.of("А123ВС77");
        GosNumber gos2 = GosNumber.of("а 123 вс 77"); // lowercase with spaces

        // Then
        assertThat(gos1).isEqualTo(gos2);
        assertThat(gos1.hashCode()).isEqualTo(gos2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenGosNumbersDiffer() {
        // Given
        GosNumber gos1 = GosNumber.of("А123ВС77");
        GosNumber gos2 = GosNumber.of("А123ВС78");

        // Then
        assertThat(gos1).isNotEqualTo(gos2);
    }

    @Test
    void shouldReturnValueInToString() {
        // Given
        GosNumber gosNumber = GosNumber.of("А123ВС77");

        // When
        String result = gosNumber.toString();

        // Then
        assertThat(result).isEqualTo("А123ВС77");
    }
}

