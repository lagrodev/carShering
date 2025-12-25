package org.example.carshering.service.impl;

import org.example.carshering.fleet.api.dto.request.create.CreateCarModelsBrand;
import org.example.carshering.fleet.api.dto.responce.BrandModelResponse;
import org.example.carshering.fleet.infrastructure.persistence.entity.Brand;
import org.example.carshering.common.exceptions.custom.NotFoundException;
import org.example.carshering.mapper.BrandMapper;
import org.example.carshering.fleet.infrastructure.persistence.repository.BrandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CarBrandServiceImpl
 */
@ExtendWith(MockitoExtension.class)
public class CarBrandServiceImplTests {

    @Mock
    private BrandMapper brandMapper;

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private CarBrandServiceImpl serviceUnderTest;


    @Test
    @DisplayName("Test createBrands returns BrandModelResponse after saving brand")
    public void givenCreateBrandRequest_whenCreateBrands_thenReturnResponse() {
        // given
        CreateCarModelsBrand request = new CreateCarModelsBrand("Toyota");
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Toyota");

        BrandModelResponse response = new BrandModelResponse( "Toyota");

        given(brandMapper.toEntity(request)).willReturn(brand);
        given(brandRepository.save(brand)).willReturn(brand);
        given(brandMapper.toDto(brand)).willReturn(response);

        // when
        BrandModelResponse result = serviceUnderTest.createBrands(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.brand()).isEqualTo("Toyota");

        verify(brandMapper).toEntity(request);
        verify(brandRepository).save(brand);
        verify(brandMapper).toDto(brand);
    }

    @Test
    @DisplayName("Test findAllBrands returns list of brand names")
    public void givenExistingBrands_whenFindAllBrands_thenReturnListOfNames() {
        // given
        Brand b1 = new Brand();
        b1.setName("BMW");
        Brand b2 = new Brand();
        b2.setName("Audi");
        Brand b3 = new Brand();
        b3.setName("Mercedes");

        given(brandRepository.findAll()).willReturn(Arrays.asList(b1, b2, b3));

        // when
        List<String> result = serviceUnderTest.findAllBrands();

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0)).isEqualTo("BMW");
        assertThat(result.get(1)).isEqualTo("Audi");
        assertThat(result.get(2)).isEqualTo("Mercedes");

        verify(brandRepository).findAll();
    }

    @Test
    @DisplayName("Test getBrandByName returns brand when found")
    public void givenExistingBrandName_whenGetBrandByName_thenReturnBrand() {
        // given
        Brand brand = new Brand();
        brand.setId(5L);
        brand.setName("Kia");

        given(brandRepository.findByNameIgnoreCase("Kia")).willReturn(Optional.of(brand));

        // when
        Brand result = serviceUnderTest.getBrandByName("Kia");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Kia");
        assertThat(result.getId()).isEqualTo(5L);

        verify(brandRepository).findByNameIgnoreCase("Kia");
    }

    @Test
    @DisplayName("Test getBrandByName throws NotFoundException when brand not found")
    public void givenNonExistingBrandName_whenGetBrandByName_thenThrowNotFoundException() {
        // given
        given(brandRepository.findByNameIgnoreCase("Unknown")).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.getBrandByName("Unknown")
        );

        verify(brandRepository).findByNameIgnoreCase("Unknown");
    }
}
