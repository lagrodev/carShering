package org.example.carshering.domain.valueobject;

import org.example.carshering.fleet.domain.valueobject.Vin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Vin Value Object.
 */
class VinTest {

    @Test
    void shouldCreateValidVin() {
        // Given
        String vinNumber = "1HGBH41JXMN109186";

        // When
        Vin vin = Vin.of(vinNumber);

        // Then
        assertThat(vin.getValue()).isEqualTo("1HGBH41JXMN109186");
    }

    @Test
    void shouldNormalizeVinToUpperCase() {
        // Given
        String vinNumber = "1hgbh41jxmn109186";

        // When
        Vin vin = Vin.of(vinNumber);

        // Then
        assertThat(vin.getValue()).isEqualTo("1HGBH41JXMN109186");
    }

    @Test
    void shouldTrimWhitespace() {
        // Given
        String vinNumber = "  1HGBH41JXMN109186  ";

        // When
        Vin vin = Vin.of(vinNumber);

        // Then
        assertThat(vin.getValue()).isEqualTo("1HGBH41JXMN109186");
    }

    @Test
    void shouldThrowExceptionWhenVinIsNull() {
        // When & Then
        assertThatThrownBy(() -> Vin.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VIN cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenVinIsBlank() {
        // When & Then
        assertThatThrownBy(() -> Vin.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VIN cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenVinIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> Vin.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("VIN cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenVinTooShort() {
        // Given - 16 characters
        String vinNumber = "1HGBH41JXMN10918";

        // When & Then
        assertThatThrownBy(() -> Vin.of(vinNumber))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("17 characters");
    }

    @Test
    void shouldThrowExceptionWhenVinTooLong() {
        // Given - 18 characters
        String vinNumber = "1HGBH41JXMN1091866";

        // When & Then
        assertThatThrownBy(() -> Vin.of(vinNumber))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("17 characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1HGBH41JXMN10918I", "1HGBH41JXMN10918O", "1HGBH41JXMN10918Q"})
    void shouldThrowExceptionWhenVinContainsInvalidCharacters(String invalidVin) {
        // When & Then
        assertThatThrownBy(() -> Vin.of(invalidVin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid characters");
    }

    @Test
    void shouldAcceptVinWithOnlyValidCharacters() {
        // Given - No I, O, Q
        String vinNumber = "1HGBH41JXMN109186";

        // When
        Vin vin = Vin.of(vinNumber);

        // Then
        assertThat(vin.getValue()).isEqualTo("1HGBH41JXMN109186");
    }

    @Test
    void shouldExtractWmi() {
        // Given
        Vin vin = Vin.of("1HGBH41JXMN109186");

        // When
        String wmi = vin.getWmi();

        // Then
        assertThat(wmi).isEqualTo("1HG");
    }

    @Test
    void shouldExtractVds() {
        // Given
        Vin vin = Vin.of("1HGBH41JXMN109186");

        // When
        String vds = vin.getVds();

        // Then
        assertThat(vds).isEqualTo("BH41JX");
    }

    @Test
    void shouldExtractVis() {
        // Given
        Vin vin = Vin.of("1HGBH41JXMN109186");

        // When
        String vis = vin.getVis();

        // Then
        assertThat(vis).isEqualTo("MN109186");
    }

    @Test
    void shouldBeEqualWhenVinsAreSame() {
        // Given
        Vin vin1 = Vin.of("1HGBH41JXMN109186");
        Vin vin2 = Vin.of("1hgbh41jxmn109186"); // lowercase

        // Then
        assertThat(vin1).isEqualTo(vin2);
        assertThat(vin1.hashCode()).isEqualTo(vin2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenVinsDiffer() {
        // Given
        Vin vin1 = Vin.of("1HGBH41JXMN109186");
        Vin vin2 = Vin.of("1HGBH41JXMN109187");

        // Then
        assertThat(vin1).isNotEqualTo(vin2);
    }

    @Test
    void shouldReturnValueInToString() {
        // Given
        Vin vin = Vin.of("1HGBH41JXMN109186");

        // When
        String result = vin.toString();

        // Then
        assertThat(result).isEqualTo("1HGBH41JXMN109186");
    }
}

