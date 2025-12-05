package org.example.carshering.service.impl;

import org.example.carshering.dto.request.create.CreateCarModelName;
import org.example.carshering.dto.response.ModelNameResponse;
import org.example.carshering.domain.entity.Model;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.mapper.ModelNameMapper;
import org.example.carshering.repository.ModelNameRepository;
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
import static org.mockito.Mockito.verify;

/**
 * Unit tests for CarModelNameServiceImpl
 */
@ExtendWith(MockitoExtension.class)
public class CarModelNameServiceImplTests {

    @Mock
    private ModelNameMapper modelNameMapper;

    @Mock
    private ModelNameRepository modelNameRepository;

    @InjectMocks
    private CarModelNameServiceImpl serviceUnderTest;


    @Test
    @DisplayName("Test createModelName returns ModelNameResponse after saving model")
    public void givenCreateModelNameRequest_whenCreateModelName_thenReturnResponse() {
        // given
        CreateCarModelName request = new CreateCarModelName("Camry");
        Model model = new Model();
        model.setId(1L);
        model.setName("Camry");

        ModelNameResponse response = new ModelNameResponse("Camry");

        given(modelNameMapper.toEntity(request)).willReturn(model);
        given(modelNameRepository.save(model)).willReturn(model);
        given(modelNameMapper.toDto(model)).willReturn(response);

        // when
        ModelNameResponse result = serviceUnderTest.createModelName(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Camry");

        verify(modelNameMapper).toEntity(request);
        verify(modelNameRepository).save(model);
        verify(modelNameMapper).toDto(model);
    }

    @Test
    @DisplayName("Test findAllModels returns list of model names")
    public void givenExistingModels_whenFindAllModels_thenReturnListOfNames() {
        // given
        Model m1 = new Model();
        m1.setName("BMW");
        Model m2 = new Model();
        m2.setName("Audi");
        Model m3 = new Model();
        m3.setName("Mercedes");

        given(modelNameRepository.findAll()).willReturn(Arrays.asList(m1, m2, m3));

        // when
        List<String> result = serviceUnderTest.findAllModels();

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0)).isEqualTo("BMW");
        assertThat(result.get(1)).isEqualTo("Audi");
        assertThat(result.get(2)).isEqualTo("Mercedes");

        verify(modelNameRepository).findAll();
    }

    @Test
    @DisplayName("Test getModelByName returns model when found")
    public void givenExistingModelName_whenGetModelByName_thenReturnModel() {
        // given
        Model model = new Model();
        model.setId(5L);
        model.setName("Kia");

        given(modelNameRepository.findByNameIgnoreCase("Kia")).willReturn(Optional.of(model));

        // when
        Model result = serviceUnderTest.getModelByName("Kia");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Kia");
        assertThat(result.getId()).isEqualTo(5L);

        verify(modelNameRepository).findByNameIgnoreCase("Kia");
    }

    @Test
    @DisplayName("Test getModelByName throws NotFoundException when model not found")
    public void givenNonExistingModelName_whenGetModelByName_thenThrowNotFoundException() {
        // given
        given(modelNameRepository.findByNameIgnoreCase("Unknown")).willReturn(Optional.empty());

        // when + then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.getModelByName("Unknown")
        );

        verify(modelNameRepository).findByNameIgnoreCase("Unknown");
    }
}