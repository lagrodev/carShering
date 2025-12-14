package org.example.carshering.service.impl;


import org.example.carshering.identity.api.dto.response.DocumentTypeResponse;
import org.example.carshering.identity.infrastructure.persistence.entity.DocumentType;
import org.example.carshering.common.exceptions.custom.DocumentTypeException;
import org.example.carshering.identity.infrastructure.persistence.repository.DocumentTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class DocumentTypeServiceImplTest {

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @InjectMocks
    private  DocumentTypeServiceImpl serviceUnderTest;

    @Test
    @DisplayName("givenExistingStates_whenGetAllStates_thenReturnMappedDtoList")
    public void givenExistingStates_whenGetAllStates_thenReturnMappedDtoList() {
        // given

        DocumentType available = new DocumentType();
        available.setId(1L);
        available.setName("PASSPORT");

        DocumentType rented = new DocumentType();
        rented.setId(2L);
        rented.setName("UDOSTOVERENIE");

        List<DocumentType> documentTypes = List.of(available, rented);

        DocumentTypeResponse availableDto = new DocumentTypeResponse(1L, "PASSPORT");
        DocumentTypeResponse rentedDto = new DocumentTypeResponse(2L, "UDOSTOVERENIE");

        given( documentTypeRepository.findAll()).willReturn(documentTypes);
        // when
        List<DocumentTypeResponse> actual = serviceUnderTest.getAllTypes();
        // then
        assert actual.size() == 2;
        assert actual.containsAll(List.of(availableDto, rentedDto));
        verify(documentTypeRepository).findAll();

    }

    @Test
    @DisplayName("givenNoStates_whenGetAllStates_thenReturnEmptyList")
    public void givenNoStates_whenGetAllStates_thenReturnEmptyList() {
        // given
        given(documentTypeRepository.findAll()).willReturn(List.of());
        // when
        List<DocumentTypeResponse> actual = serviceUnderTest.getAllTypes();
        // then
        assert actual.isEmpty();
        verify(documentTypeRepository).findAll();
    }

    @Test
    @DisplayName("givenId_whenGetById_thenReturnDocumentType")
    public void givenId_whenGetById_thenReturnDocumentType() {
        // given
        Long id = 1L;
        DocumentType documentType = DocumentType.builder().name("PASSPORT").id(id).build();

        given(documentTypeRepository.findById(id)).willReturn(java.util.Optional.ofNullable(documentType));
        // when
        DocumentType actual = serviceUnderTest.getById(id);
        // then
        assert actual.equals(documentType);
        verify(documentTypeRepository).findById(id);
    }

    @Test
    @DisplayName("givenIdIncorrect_whenGetById_thenThrowNotFoundException")
    public void givenIdIncorrect_whenGetById_thenThrowNotFoundException() {
        // given
        Long id = 1L;

        given(documentTypeRepository.findById(id)).willReturn(java.util.Optional.empty());
        // when + then

        assertThrows(
                DocumentTypeException.class,
                () -> serviceUnderTest.getById(id),
                "Document type not found"
        );
        verify(documentTypeRepository).findById(id);
    }


}
