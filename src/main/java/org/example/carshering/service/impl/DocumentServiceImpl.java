package org.example.carshering.service.impl;


import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.create.CreateDocumentRequest;
import org.example.carshering.dto.request.update.UpdateDocumentRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.domain.entity.Client;
import org.example.carshering.domain.entity.Document;
import org.example.carshering.domain.entity.DocumentType;
import org.example.carshering.exceptions.custom.AlreadyExistsException;
import org.example.carshering.exceptions.custom.BannedClientAccessException;
import org.example.carshering.exceptions.custom.NotFoundException;
import org.example.carshering.mapper.DocumentMapper;
import org.example.carshering.repository.DocumentRepository;
import org.example.carshering.service.interfaces.DocumentService;
import org.example.carshering.service.domain.ClientServiceHelper;
import org.example.carshering.service.domain.DocumentTypeServiceHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentTypeServiceHelper documentTypeService;
    private final DocumentMapper documentMapper;
    private final DocumentRepository documentRepository;

    private final ClientServiceHelper clientService;

    @Override
    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest request, Long userId) {

        if (request == null) {
            throw new NotFoundException("CreateDocumentRequest cannot be null");
        }


        if (documentRepository.existsByClientIdAndDeletedFalse(userId)) {
            throw new AlreadyExistsException("Document already exists");
        }

        if (documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(request.number(), request.series())) {
            throw new BannedClientAccessException("Document was banned");
        }

        boolean exists = documentRepository.existsBySeriesAndNumber(request.series(), request.number());

        if (exists) {
            throw new AlreadyExistsException("Document already exists");
        }

        Client client = clientService.getEntity(userId);

        DocumentType type = documentTypeService.getById(request.documentTypeId());

        Document doc = documentMapper.toEntity(request);

        doc.setClient(client);
        doc.setDocumentType(type);

        return documentMapper.toDto(documentRepository.save(doc));
    }

    @Override
    public boolean hasDocument(Long userId) {
        return documentRepository.existsByClientIdAndDeletedFalse(userId);
    }

    @Override
    public DocumentResponse findDocument(Long userId) {
        return documentRepository.findByClientIdAndDeletedFalse(userId)
                .map(documentMapper::toDto)
                .orElse(null);
    }

    // todo проверить, что нельзя обновить тип документа

    @Override
    @Transactional
    public DocumentResponse updateDocument(Long userId, UpdateDocumentRequest request) {

        if (request == null) {
            throw new NotFoundException("UpdateDocumentRequest cannot be null");
        }

        Document document = documentRepository.findByClientIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("Document not found"));


        if (documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(request.number(), request.series())) {
            throw new BannedClientAccessException("Document was banned");
        }

        // если у типа есть series, но нет number, то надо провериьт, что серия и номер уникальны
        boolean seriesChanged = request.series() != null && !request.series().equals(document.getSeries());
        boolean numberChanged = request.number() != null && !request.number().equals(document.getNumber());

        if (seriesChanged || numberChanged) {
            String newSeries = seriesChanged ? request.series() : document.getSeries();
            String newNumber = numberChanged ? request.number() : document.getNumber();

            boolean exists = documentRepository.existsBySeriesAndNumber(newSeries, newNumber);

            if (exists) {
                throw new AlreadyExistsException("Document already exists");
            }
        }

        documentMapper.update(document, request);

        document.setVerified(false);

        return documentMapper.toDto(documentRepository.save(document));
    }

    // todo сделать поле, чтобы смотреть, кто подтвердил документ (админ)
    @Override
    @Transactional
    public void verifyDocument(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        doc.setVerified(true);
        documentRepository.save(doc);
    }

    @Override
    public Page<DocumentResponse> getAllDocuments(boolean onlyUnverified, Pageable pageable) {
        Page<Document> documents = onlyUnverified
                ? documentRepository.findByVerifiedIsFalse(pageable)
                : documentRepository.findAll(pageable);

        return documents

                .map(documentMapper::toDto)
                ;
    }

    @Override
    @Transactional
    public void deleteDocument(Long userId) {

        Document document = documentRepository.findByClientIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        document.setDeleted(true);
        documentRepository.save(document);
    }



}
