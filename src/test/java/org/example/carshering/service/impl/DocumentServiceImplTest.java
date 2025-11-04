package org.example.carshering.service.impl;


import org.example.carshering.dto.request.create.CreateDocumentRequest;
import org.example.carshering.dto.request.update.UpdateDocumentRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Document;
import org.example.carshering.entity.DocumentType;
import org.example.carshering.exceptions.custom.AlreadyExistsException;
import org.example.carshering.exceptions.custom.BannedClientAccessException;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.mapper.DocumentMapper;
import org.example.carshering.repository.DocumentRepository;
import org.example.carshering.service.ClientService;
import org.example.carshering.service.DocumentTypeService;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentTypeService documentTypeService;
    @Mock
    private DocumentMapper documentMapper;
    @Mock
    private ClientService clientService;

    @InjectMocks
    private DocumentServiceImpl serviceUnderTest;

    private DataUtils dataUtils = new DataUtils();

    @Test
    @DisplayName("Test save document functionality")
    public void givenDocumentToSave_whenSaveDocument_thenRepositoryIsCalled() {
        // given
        String series = "1123";
        String number = "3334";
        String issuingAuthority = "AUTO";

        CreateDocumentRequest request = dataUtils.createDocumentRequestTransient(series, number, issuingAuthority);

        String docType = "AVAILABLE";

        DocumentType documentType = DataUtils.getDocumentTypePersisted(docType);

        Client client = dataUtils.createAndSaveClient("test", "test@mail");
        Document documentEntity = spy(dataUtils.createDocumentTransient(series, number, issuingAuthority, false));

        Document savedDocument = dataUtils.createDocumentPersisted(client, documentType, series, number, issuingAuthority, false);
        DocumentResponse response = DataUtils.documentResponsePersisted(docType, series, number, issuingAuthority, false);

        given(documentRepository.existsByClientIdAndDeletedFalse(anyLong())).willReturn(false);
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(request.number(), request.series())).willReturn(false);
        given(documentRepository.existsBySeriesAndNumber(request.series(), request.number())).willReturn(false);

        given(documentMapper.toEntity(request)).willReturn(documentEntity);
        given(documentTypeService.getById(request.documentTypeId())).willReturn(documentType);
        given(clientService.getEntity(anyLong())).willReturn(client);
        given(documentMapper.toDto(savedDocument)).willReturn(response);
        given(documentRepository.save(documentEntity)).willReturn(savedDocument);

        // when
        DocumentResponse actual = serviceUnderTest.createDocument(request, 1L);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.series()).isEqualTo(series);
        assertThat(actual.number()).isEqualTo(number);
        assertThat(actual.issuingAuthority()).isEqualTo(issuingAuthority);
        assertThat(actual.verified()).isEqualTo(false);
        assertThat(actual.documentType()).isEqualTo("AVAILABLE");

        verify(documentMapper).toEntity(request);
        verify(documentRepository).save(documentEntity);
        verify(documentMapper).toDto(savedDocument);

        verify(documentEntity).setClient(client);
        verify(documentEntity).setDocumentType(documentType);
        verify(documentRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Test save document with incorrect id throws exception")
    public void givenDocumentToSaveWightIncorrectClientId_whenSaveDocument_thenThrowException() {
        // given
        String series = "1123";
        String number = "3334";
        String issuingAuthority = "AUTO";

        CreateDocumentRequest request = dataUtils.createDocumentRequestTransient(series, number, issuingAuthority);

        given(documentRepository.existsByClientIdAndDeletedFalse(anyLong())).willReturn(true);

        // when
        assertThrows(
                AlreadyExistsException.class,
                () -> serviceUnderTest.createDocument(request, 1L),
                "Document already exists"
        );

        verify(documentRepository, never()).save(any());
        verify(documentMapper, never()).toEntity(any());
        verify(documentMapper, never()).toDto(any());
        verify(documentTypeService, never()).getById(any());
        verify(clientService, never()).getEntity(any());

    }

    @Test
    @DisplayName("Test save document with belongs a blocked client throws exception")
    public void givenDocumentToSaveBelongsToBlockedClient_whenSaveDocument_thenThrowException() {
        // given
        String series = "1123";
        String number = "3334";
        String issuingAuthority = "AUTO";

        CreateDocumentRequest request = dataUtils.createDocumentRequestTransient(series, number, issuingAuthority);

        given(documentRepository.existsByClientIdAndDeletedFalse(anyLong())).willReturn(false);
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(request.number(), request.series())).willReturn(true);
        assertThrows(
                BannedClientAccessException.class,
                () -> serviceUnderTest.createDocument(request, 1L),
                "Document was banned"
        );

        verify(documentRepository, never()).save(any());
        verify(documentMapper, never()).toEntity(any());
        verify(documentMapper, never()).toDto(any());
        verify(documentTypeService, never()).getById(any());
        verify(clientService, never()).getEntity(any());

    }

    @Test
    @DisplayName("Test save document with duplicate series and number client throws exception")
    public void givenDocumentToSaveDuplicateSeriesAndNumber_whenSaveDocument_thenThrowException() {
        // given
        String series = "1123";
        String number = "3334";
        String issuingAuthority = "AUTO";

        CreateDocumentRequest request = dataUtils.createDocumentRequestTransient(series, number, issuingAuthority);

        given(documentRepository.existsByClientIdAndDeletedFalse(anyLong())).willReturn(false);
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(request.number(), request.series())).willReturn(false);
        given(documentRepository.existsBySeriesAndNumber(request.series(), request.number())).willReturn(true);
        // when
        assertThrows(
                AlreadyExistsException.class,
                () -> serviceUnderTest.createDocument(request, 1L),
                "Document already exists"
        );
        // then
        verify(documentRepository, never()).save(any());
        verify(documentMapper, never()).toEntity(any());
        verify(documentMapper, never()).toDto(any());
        verify(documentTypeService, never()).getById(any());
        verify(clientService, never()).getEntity(any());

    }

    @Test
    @DisplayName("Test save document with null request throws NotFoundException")
    public void givenNullRequest_whenSaveDocument_thenThrowNullPointerException() {
        // given
        Long userId = 1L;

        // when & then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.createDocument(null, userId),
                "CreateDocumentRequest cannot be null"
        );

        verify(documentRepository, never()).save(any());
        verify(documentMapper, never()).toEntity(any());
        verify(documentMapper, never()).toDto(any());
        verify(documentTypeService, never()).getById(any());
        verify(clientService, never()).getEntity(any());
    }


    @Test
    @DisplayName("Test update document with null request throws NotFoundException")
    public void givenNullRequest_whenUpdateDocument_thenThrowNullPointerException() {
        // given
        Long userId = 1L;

        // when & then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.updateDocument(userId, null),
                "UpdateDocumentRequest cannot be null"
        );

        verify(documentRepository, never()).save(any());

        verify(documentRepository, never()).findByClientIdAndDeletedFalse(any());
    }

    @Test
    @DisplayName("Test updateDocument when series and number unchanged should not check duplicates")
    public void givenSameSeriesAndNumber_whenUpdateDocument_thenUpdateWithoutDuplicateCheck() {
        // given
        Long userId = 1L;
        String series = "1111";
        String number = "2222";
        String issuingAuthority = "AUTO";

        Document existingDocument = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                series, number, issuingAuthority, false
        );

        UpdateDocumentRequest updateRequest = dataUtils.createUpdateDocumentRequest(series, number, "NEW AUTH");

        Document updatedDocument = existingDocument;
        DocumentResponse updatedResponse = DataUtils.documentResponsePersisted(
                "AVAILABLE", series, number, "NEW AUTH", false
        );

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existingDocument));
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(updateRequest.number(), updateRequest.series()))
                .willReturn(false);
        doNothing().when(documentMapper).update(existingDocument, updateRequest);
        given(documentRepository.save(existingDocument)).willReturn(updatedDocument);
        given(documentMapper.toDto(updatedDocument)).willReturn(updatedResponse);

        // when
        DocumentResponse actual = serviceUnderTest.updateDocument(userId, updateRequest);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.series()).isEqualTo(series);
        assertThat(actual.number()).isEqualTo(number);
        verify(documentRepository, never()).existsBySeriesAndNumber(any(), any());
        verify(documentMapper).update(existingDocument, updateRequest);
        verify(documentRepository).save(existingDocument);
    }
    @Test
    @DisplayName("Test updateDocument when repository returns null")
    public void givenRepositoryReturnsNull_whenUpdateDocument_thenReturnNullDto() {
        // given
        Long userId = 1L;
        String series = "1111";
        String number = "2222";
        String issuingAuthority = "AUTO";

        Document existingDocument = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                series, number, issuingAuthority, false
        );

        UpdateDocumentRequest updateRequest = dataUtils.createUpdateDocumentRequest(series, number, "AUTO");

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existingDocument));
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(updateRequest.number(), updateRequest.series()))
                .willReturn(false);
        doNothing().when(documentMapper).update(existingDocument, updateRequest);
        given(documentRepository.save(existingDocument)).willReturn(null);
        given(documentMapper.toDto(null)).willReturn(null);

        // when
        DocumentResponse actual = serviceUnderTest.updateDocument(userId, updateRequest);

        // then
        assertThat(actual).isNull();
        verify(documentRepository).save(existingDocument);
        verify(documentMapper).toDto(null);
    }




    @Test
    @DisplayName("Test save document when clientService throws exception")
    public void givenClientServiceThrowsException_whenSaveDocument_thenThrowException() {
        // given
        String series = "1123";
        String number = "3334";
        String issuingAuthority = "AUTO";

        CreateDocumentRequest request = dataUtils.createDocumentRequestTransient(series, number, issuingAuthority);

        given(documentRepository.existsByClientIdAndDeletedFalse(anyLong())).willReturn(false);
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(request.number(), request.series())).willReturn(false);
        given(documentRepository.existsBySeriesAndNumber(request.series(), request.number())).willReturn(false);

        given(clientService.getEntity(anyLong())).willThrow(new NotFoundException("Client not found"));

        // when & then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.createDocument(request, 1L),
                "Client not found"
        );

        verify(documentRepository, never()).save(any());
        verify(documentMapper, never()).toDto(any());
        verify(documentTypeService, never()).getById(any());
    }


    @Test
    @DisplayName("Test save document when documentMapper.toEntity returns null")
    public void givenMapperReturnsNull_whenSaveDocument_thenThrowException() {
        // given
        String series = "1123";
        String number = "3334";
        String issuingAuthority = "AUTO";

        CreateDocumentRequest request = dataUtils.createDocumentRequestTransient(series, number, issuingAuthority);

        given(documentRepository.existsByClientIdAndDeletedFalse(anyLong())).willReturn(false);
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(request.number(), request.series())).willReturn(false);
        given(documentRepository.existsBySeriesAndNumber(request.series(), request.number())).willReturn(false);

        given(documentMapper.toEntity(request)).willReturn(null);

        // when & then
        assertThrows(
                NullPointerException.class,
                () -> serviceUnderTest.createDocument(request, 1L)
        );

        verify(documentRepository, never()).save(any());
        verify(documentMapper, never()).toDto(any());
    }



    @Test
    @DisplayName("Test hasDocument returns true when document exists and not deleted")
    public void givenExistingActiveDocument_whenHasDocument_thenReturnTrue() {
        // given
        Long userId = 1L;
        given(documentRepository.existsByClientIdAndDeletedFalse(userId))
                .willReturn(true);

        // when
        boolean result = serviceUnderTest.hasDocument(userId);

        // then
        assertThat(result).isTrue();
        verify(documentRepository).existsByClientIdAndDeletedFalse(userId);
    }
    @Test
    @DisplayName("Test hasDocument returns false when document not found or deleted")
    public void givenNoActiveDocument_whenHasDocument_thenReturnFalse() {
        // given
        Long userId = 2L;
        given(documentRepository.existsByClientIdAndDeletedFalse(userId))
                .willReturn(false);

        // when
        boolean result = serviceUnderTest.hasDocument(userId);

        // then
        assertThat(result).isFalse();
        verify(documentRepository).existsByClientIdAndDeletedFalse(userId);
    }


    @Test
    @DisplayName("Test findDocument returns DTO when document exists and not deleted")
    public void givenExistingActiveDocument_whenFindDocument_thenReturnDto() {
        // given
        Long userId = 1L;
        String series = "1123";
        String number = "3334";
        String issuingAuthority = "AUTO";

        CreateDocumentRequest request = dataUtils.createDocumentRequestTransient(series, number, issuingAuthority);

        String docType = "AVAILABLE";

        Document documentEntity = dataUtils.createDocumentTransient(series, number, issuingAuthority, false);

        DocumentResponse responseDto = DataUtils.documentResponsePersisted(docType, series, number, issuingAuthority, false);

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(documentEntity));
        given(documentMapper.toDto(documentEntity))
                .willReturn(responseDto);

        // when
        DocumentResponse actual = serviceUnderTest.findDocument(userId);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(1L);
        assertThat(actual.id()).isEqualTo(1L);
        verify(documentRepository).findByClientIdAndDeletedFalse(userId);
        verify(documentMapper).toDto(documentEntity);
    }


    @Test
    @DisplayName("Test findDocument returns null when document not found or deleted")
    public void givenNoActiveDocument_whenFindDocument_thenReturnNull() {
        // given
        Long userId = 99L;
        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.empty());

        // when
        DocumentResponse actual = serviceUnderTest.findDocument(userId);

        // then
        assertThat(actual).isNull();
        verify(documentRepository).findByClientIdAndDeletedFalse(userId);
        verify(documentMapper, never()).toDto(any(Document.class));
    }


    @Test
    @DisplayName("Test updateDocument updates existing document successfully")
    public void givenValidRequest_whenUpdateDocument_thenUpdateAndReturnDto() {
        // given
        Long userId = 1L;
        String oldSeries = "1111";
        String oldNumber = "2222";
        String newSeries = "3333";
        String newNumber = "4444";
        String issuingAuthority = "AUTO";

        Document existingDocument = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("user", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                oldSeries, oldNumber, issuingAuthority, false
        );

        UpdateDocumentRequest updateRequest = dataUtils.createUpdateDocumentRequest(newSeries, newNumber, issuingAuthority);

        Document updatedDocument = dataUtils.createDocumentPersisted(
                existingDocument.getClient(),
                existingDocument.getDocumentType(),
                newSeries, newNumber, issuingAuthority, false
        );

        DocumentResponse updatedResponse = DataUtils.documentResponsePersisted(
                "AVAILABLE", newSeries, newNumber, issuingAuthority, false
        );

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existingDocument));
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(updateRequest.number(), updateRequest.series()))
                .willReturn(false);
        given(documentRepository.existsBySeriesAndNumber(newSeries, newNumber))
                .willReturn(false);
        doNothing().when(documentMapper).update(existingDocument, updateRequest);
        given(documentRepository.save(existingDocument)).willReturn(updatedDocument);
        given(documentMapper.toDto(updatedDocument)).willReturn(updatedResponse);

        // when
        DocumentResponse actual = serviceUnderTest.updateDocument(userId, updateRequest);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.series()).isEqualTo(newSeries);
        assertThat(actual.number()).isEqualTo(newNumber);
        assertThat(actual.verified()).isFalse();

        verify(documentRepository).findByClientIdAndDeletedFalse(userId);
        verify(documentMapper).update(existingDocument, updateRequest);
        verify(documentRepository).save(existingDocument);
        verify(documentMapper).toDto(updatedDocument);
    }


    @Test
    @DisplayName("Test updateDocument when number changed and request.series is null then use existing series and update")
    public void givenRequestSeriesIsNullAndNumberChanged_whenUpdateDocument_thenUseExistingSeriesAndUpdate() {
        // given
        Long userId = 1L;
        String oldSeries = "1111";
        String oldNumber = "2222";
        String newNumber = "9999";
        String issuingAuthority = "AUTO";

        Document existingDocument = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                oldSeries, oldNumber, issuingAuthority, false
        );

        // создаём запрос с number != null, но series == null
        UpdateDocumentRequest updateRequest = dataUtils.createUpdateDocumentRequest(null, newNumber, "NEW AUTH");

        Document updatedDocument = dataUtils.createDocumentPersisted(
                existingDocument.getClient(),
                existingDocument.getDocumentType(),
                oldSeries, newNumber, "NEW AUTH", false
        );

        DocumentResponse updatedResponse = DataUtils.documentResponsePersisted(
                "AVAILABLE", oldSeries, newNumber, "NEW AUTH", false
        );

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existingDocument));
        // проверка на заблокированного клиента — передаём request.number() и request.series() (series == null)
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(updateRequest.number(), updateRequest.series()))
                .willReturn(false);
        // поскольку series в запросе null, должна проверяться комбинация (oldSeries, newNumber)
        given(documentRepository.existsBySeriesAndNumber(oldSeries, newNumber))
                .willReturn(false);
        doNothing().when(documentMapper).update(existingDocument, updateRequest);
        given(documentRepository.save(existingDocument)).willReturn(updatedDocument);
        given(documentMapper.toDto(updatedDocument)).willReturn(updatedResponse);

        // when
        DocumentResponse actual = serviceUnderTest.updateDocument(userId, updateRequest);

        // then
        assertThat(actual).isNotNull();
        // серия должна остаться старой, т.к. в запросе она была null
        assertThat(actual.series()).isEqualTo(oldSeries);
        assertThat(actual.number()).isEqualTo(newNumber);
        assertThat(actual.verified()).isFalse();

        verify(documentRepository).existsBySeriesAndNumber(oldSeries, newNumber);
        verify(documentMapper).update(existingDocument, updateRequest);
        verify(documentRepository).save(existingDocument);
    }

    @Test
    @DisplayName("Test updateDocument when series changed and request.number is null then use existing number and update")
    public void givenSeriesChangedAndRequestNumberIsNull_whenUpdateDocument_thenUseExistingNumberAndUpdate() {
        // given
        Long userId = 1L;
        String oldSeries = "1111";
        String oldNumber = "2222";
        String newSeries = "3333";
        String issuingAuthority = "AUTO";

        Document existingDocument = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                oldSeries, oldNumber, issuingAuthority, false
        );

        // создаём запрос с новой серией, но number == null
        UpdateDocumentRequest updateRequest = dataUtils.createUpdateDocumentRequest(newSeries, null, "NEW AUTH");

        Document updatedDocument = dataUtils.createDocumentPersisted(
                existingDocument.getClient(),
                existingDocument.getDocumentType(),
                newSeries, oldNumber, "NEW AUTH", false
        );

        DocumentResponse updatedResponse = DataUtils.documentResponsePersisted(
                "AVAILABLE", newSeries, oldNumber, "NEW AUTH", false
        );

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existingDocument));
        // проверка на заблокированного клиента — передаём request.number() == null и request.series()
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(updateRequest.number(), updateRequest.series()))
                .willReturn(false);
        // поскольку number в запросе null, должна проверяться комбинация (newSeries, oldNumber)
        given(documentRepository.existsBySeriesAndNumber(newSeries, oldNumber))
                .willReturn(false);
        doNothing().when(documentMapper).update(existingDocument, updateRequest);
        given(documentRepository.save(existingDocument)).willReturn(updatedDocument);
        given(documentMapper.toDto(updatedDocument)).willReturn(updatedResponse);

        // when
        DocumentResponse actual = serviceUnderTest.updateDocument(userId, updateRequest);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.series()).isEqualTo(newSeries);
        // номер должен остаться старым, т.к. в запросе он был null
        assertThat(actual.number()).isEqualTo(oldNumber);
        assertThat(actual.verified()).isFalse();

        verify(documentRepository).existsBySeriesAndNumber(newSeries, oldNumber);
        verify(documentMapper).update(existingDocument, updateRequest);
        verify(documentRepository).save(existingDocument);
    }


    @Test
    @DisplayName("Test updateDocument when only series changed then check duplicates and update successfully")
    public void givenOnlySeriesChanged_whenUpdateDocument_thenCheckDuplicateAndUpdate() {
        // given
        Long userId = 1L;
        String oldSeries = "1111";
        String oldNumber = "2222";
        String newSeries = "3333";
        String issuingAuthority = "AUTO";

        Document existingDocument = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                oldSeries, oldNumber, issuingAuthority, false
        );

        UpdateDocumentRequest updateRequest = dataUtils.createUpdateDocumentRequest(newSeries, oldNumber, "NEW AUTH");

        Document updatedDocument = dataUtils.createDocumentPersisted(
                existingDocument.getClient(),
                existingDocument.getDocumentType(),
                newSeries, oldNumber, "NEW AUTH", false
        );

        DocumentResponse updatedResponse = DataUtils.documentResponsePersisted(
                "AVAILABLE", newSeries, oldNumber, "NEW AUTH", false
        );

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existingDocument));
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(updateRequest.number(), updateRequest.series()))
                .willReturn(false);
        given(documentRepository.existsBySeriesAndNumber(newSeries, oldNumber))
                .willReturn(false);
        doNothing().when(documentMapper).update(existingDocument, updateRequest);
        given(documentRepository.save(existingDocument)).willReturn(updatedDocument);
        given(documentMapper.toDto(updatedDocument)).willReturn(updatedResponse);

        // when
        DocumentResponse actual = serviceUnderTest.updateDocument(userId, updateRequest);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.series()).isEqualTo(newSeries);
        assertThat(actual.number()).isEqualTo(oldNumber);
        assertThat(actual.verified()).isFalse();

        verify(documentRepository).existsBySeriesAndNumber(newSeries, oldNumber);
        verify(documentMapper).update(existingDocument, updateRequest);
        verify(documentRepository).save(existingDocument);
    }
    @Test
    @DisplayName("Test updateDocument when only number changed then check duplicates and update successfully")
    public void givenOnlyNumberChanged_whenUpdateDocument_thenCheckDuplicateAndUpdate() {
        // given
        Long userId = 1L;
        String oldSeries = "1111";
        String oldNumber = "2222";
        String newNumber = "9999";
        String issuingAuthority = "AUTO";

        Document existingDocument = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                oldSeries, oldNumber, issuingAuthority, false
        );

        UpdateDocumentRequest updateRequest = dataUtils.createUpdateDocumentRequest(oldSeries, newNumber, "NEW AUTH");

        Document updatedDocument = dataUtils.createDocumentPersisted(
                existingDocument.getClient(),
                existingDocument.getDocumentType(),
                oldSeries, newNumber, "NEW AUTH", false
        );

        DocumentResponse updatedResponse = DataUtils.documentResponsePersisted(
                "AVAILABLE", oldSeries, newNumber, "NEW AUTH", false
        );

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existingDocument));
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(updateRequest.number(), updateRequest.series()))
                .willReturn(false);
        given(documentRepository.existsBySeriesAndNumber(oldSeries, newNumber))
                .willReturn(false);
        doNothing().when(documentMapper).update(existingDocument, updateRequest);
        given(documentRepository.save(existingDocument)).willReturn(updatedDocument);
        given(documentMapper.toDto(updatedDocument)).willReturn(updatedResponse);

        // when
        DocumentResponse actual = serviceUnderTest.updateDocument(userId, updateRequest);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.series()).isEqualTo(oldSeries);
        assertThat(actual.number()).isEqualTo(newNumber);
        assertThat(actual.verified()).isFalse();

        verify(documentRepository).existsBySeriesAndNumber(oldSeries, newNumber);
        verify(documentMapper).update(existingDocument, updateRequest);
        verify(documentRepository).save(existingDocument);
    }
    @Test
    @DisplayName("Test updateDocument when mapper.update throws exception then propagate it")
    public void givenMapperThrowsException_whenUpdateDocument_thenThrowException() {
        // given
        Long userId = 1L;
        String series = "1111";
        String number = "2222";
        String issuingAuthority = "AUTO";

        Document existingDocument = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                series, number, issuingAuthority, false
        );

        UpdateDocumentRequest updateRequest = dataUtils.createUpdateDocumentRequest(series, number, "NEW AUTH");

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existingDocument));
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(updateRequest.number(), updateRequest.series()))
                .willReturn(false);

        doThrow(new RuntimeException("Mapper failed"))
                .when(documentMapper).update(existingDocument, updateRequest);

        // when & then
        assertThrows(
                RuntimeException.class,
                () -> serviceUnderTest.updateDocument(userId, updateRequest),
                "Mapper failed"
        );

        verify(documentRepository, never()).save(any());
    }
    @Test
    @DisplayName("Test updateDocument always sets verified to false after update")
    public void givenVerifiedDocument_whenUpdateDocument_thenSetVerifiedFalse() {
        // given
        Long userId = 1L;
        String series = "1234";
        String number = "5678";
        String issuingAuthority = "AUTO";

        Document existingDocument = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                series, number, issuingAuthority, true
        );
        existingDocument.setVerified(true);

        UpdateDocumentRequest updateRequest = dataUtils.createUpdateDocumentRequest(series, number, "NEW AUTH");

        Document updatedDocument = existingDocument;
        updatedDocument.setVerified(false);

        DocumentResponse response = DataUtils.documentResponsePersisted(
                "AVAILABLE", series, number, "NEW AUTH", false
        );

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existingDocument));
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(updateRequest.number(), updateRequest.series()))
                .willReturn(false);
        doNothing().when(documentMapper).update(existingDocument, updateRequest);
        given(documentRepository.save(existingDocument)).willReturn(updatedDocument);
        given(documentMapper.toDto(updatedDocument)).willReturn(response);

        // when
        DocumentResponse actual = serviceUnderTest.updateDocument(userId, updateRequest);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual.verified()).isFalse();
        verify(documentRepository).save(existingDocument);
    }


    @Test
    @DisplayName("Test updateDocument throws exception when document not found")
    public void givenNoExistingDocument_whenUpdateDocument_thenThrowException() {
        // given
        Long userId = 99L;
        UpdateDocumentRequest request = dataUtils.createUpdateDocumentRequest("1234", "5678", "AUTO");

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.updateDocument(userId, request),
                "Document not found"
        );

        verify(documentRepository).findByClientIdAndDeletedFalse(userId);
        verify(documentMapper, never()).update(any(), any());
        verify(documentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test updateDocument throws exception when document is banned")
    public void givenBannedDocument_whenUpdateDocument_thenThrowBannedClientAccessException() {
        // given
        Long userId = 1L;
        UpdateDocumentRequest request = dataUtils.createUpdateDocumentRequest("1234", "5678", "AUTO");
        Document existing = dataUtils.createDocumentTransient("1111", "2222", "AUTO", false);

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existing));
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(request.number(), request.series()))
                .willReturn(true);

        // when & then
        assertThrows(
                BannedClientAccessException.class,
                () -> serviceUnderTest.updateDocument(userId, request),
                "Document was banned"
        );

        verify(documentRepository, never()).save(any());
        verify(documentMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Test updateDocument throws exception when duplicate series and number exist")
    public void givenDuplicateSeriesAndNumber_whenUpdateDocument_thenThrowAlreadyExistsException() {
        // given
        Long userId = 1L;
        String series = "9999";
        String number = "8888";
        Document existing = dataUtils.createDocumentTransient("1111", "2222", "AUTO", false);
        UpdateDocumentRequest request = dataUtils.createUpdateDocumentRequest(series, number, "AUTO");

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(existing));
        given(documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(request.number(), request.series()))
                .willReturn(false);
        given(documentRepository.existsBySeriesAndNumber(series, number))
                .willReturn(true);

        // when & then
        assertThrows(
                AlreadyExistsException.class,
                () -> serviceUnderTest.updateDocument(userId, request),
                "Document already exists"
        );

        verify(documentRepository, never()).save(any());
        verify(documentMapper, never()).toDto(any());
    }


    @Test
    @DisplayName("Test verifyDocument sets verified true successfully")
    public void givenExistingDocument_whenVerifyDocument_thenSetVerifiedTrue() {
        // given
        Long documentId = 1L;
        Document doc = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                "1111", "2222", "AUTO", false
        );

        given(documentRepository.findById(documentId))
                .willReturn(Optional.of(doc));

        // when
        serviceUnderTest.verifyDocument(documentId);

        // then
        assertThat(doc.isVerified()).isTrue();
        verify(documentRepository).findById(documentId);
        verify(documentRepository).save(doc);
    }


    @Test
    @DisplayName("Test verifyDocument does not fail when document already verified")
    public void givenAlreadyVerifiedDocument_whenVerifyDocument_thenNoException() {
        // given
        Long documentId = 1L;
        Document doc = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                "1111", "2222", "AUTO", true
        );
        doc.setVerified(true);

        given(documentRepository.findById(documentId))
                .willReturn(Optional.of(doc));

        // when
        serviceUnderTest.verifyDocument(documentId);

        // then
        assertThat(doc.isVerified()).isTrue();
        verify(documentRepository).findById(documentId);
        verify(documentRepository).save(doc);
    }


    @Test
    @DisplayName("Test verifyDocument throws exception when document not found")
    public void givenNoDocument_whenVerifyDocument_thenThrowException() {
        // given
        Long documentId = 404L;
        given(documentRepository.findById(documentId))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.verifyDocument(documentId),
                "Document not found"
        );

        verify(documentRepository, never()).save(any());
    }


    @Test
    @DisplayName("Test getAllDocuments returns all documents when onlyUnverified = false")
    public void givenAllDocumentsRequested_whenGetAllDocuments_thenReturnAllDtos() {
        // given
        Document doc1 = dataUtils.createDocumentTransient("1111", "2222", "AUTO", true);
        Document doc2 = dataUtils.createDocumentTransient("3333", "4444", "MVD", false);

        given(documentRepository.findAll()).willReturn(java.util.List.of(doc1, doc2));
        given(documentMapper.toDto(doc1)).willReturn(DataUtils.documentResponsePersisted("AVAILABLE", "1111", "2222", "AUTO", true));
        given(documentMapper.toDto(doc2)).willReturn(DataUtils.documentResponsePersisted("AVAILABLE", "3333", "4444", "MVD", false));

        // when
        var result = serviceUnderTest.getAllDocuments(false);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        verify(documentRepository).findAll();
        verify(documentMapper, times(2)).toDto(any());
    }

    @Test
    @DisplayName("Test getAllDocuments returns empty list when no documents found")
    public void givenNoDocuments_whenGetAllDocuments_thenReturnEmptyList() {
        // given
        given(documentRepository.findAll()).willReturn(java.util.List.of());

        // when
        var result = serviceUnderTest.getAllDocuments(false);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);
        verify(documentRepository).findAll();
        verify(documentMapper, never()).toDto(any());
    }



    @Test
    @DisplayName("Test getAllDocuments returns only unverified documents when onlyUnverified = true")
    public void givenOnlyUnverifiedRequested_whenGetAllDocuments_thenReturnUnverifiedDtos() {
        // given
        Document doc = dataUtils.createDocumentTransient("1111", "2222", "AUTO", false);
        given(documentRepository.findByVerifiedIsFalse()).willReturn(java.util.List.of(doc));
        given(documentMapper.toDto(doc)).willReturn(DataUtils.documentResponsePersisted("AVAILABLE", "1111", "2222", "AUTO", false));

        // when
        var result = serviceUnderTest.getAllDocuments(true);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).verified()).isFalse();
        verify(documentRepository).findByVerifiedIsFalse();
        verify(documentMapper).toDto(doc);
    }


    @Test
    @DisplayName("Test deleteDocument sets deleted true and saves")
    public void givenExistingDocument_whenDeleteDocument_thenSetDeletedTrue() {
        // given
        Long userId = 1L;
        Document doc = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                "1111", "2222", "AUTO", false
        );

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(doc));

        // when
        serviceUnderTest.deleteDocument(userId);

        // then
        assertThat(doc.isDeleted()).isTrue();
        verify(documentRepository).findByClientIdAndDeletedFalse(userId);
        verify(documentRepository).save(doc);
    }

    @Test
    @DisplayName("Test deleteDocument when document already deleted does not throw")
    public void givenAlreadyDeletedDocument_whenDeleteDocument_thenNoException() {
        // given
        Long userId = 1L;
        Document doc = dataUtils.createDocumentPersisted(
                dataUtils.createAndSaveClient("client", "mail@mail"),
                DataUtils.getDocumentTypePersisted("AVAILABLE"),
                "1111", "2222", "AUTO", false
        );
        doc.setDeleted(true);

        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.of(doc));

        // when
        serviceUnderTest.deleteDocument(userId);

        // then
        assertThat(doc.isDeleted()).isTrue();
        verify(documentRepository).findByClientIdAndDeletedFalse(userId);
        verify(documentRepository).save(doc);
    }


    @Test
    @DisplayName("Test deleteDocument throws exception when document not found")
    public void givenNoDocument_whenDeleteDocument_thenThrowException() {
        // given
        Long userId = 999L;
        given(documentRepository.findByClientIdAndDeletedFalse(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(
                NotFoundException.class,
                () -> serviceUnderTest.deleteDocument(userId),
                "Document not found"
        );

        verify(documentRepository, never()).save(any());
    }


}
