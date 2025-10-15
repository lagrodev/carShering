package org.example.carshering.service.impl;


import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.request.CreateDocumentRequest;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.entity.Client;
import org.example.carshering.entity.Document;
import org.example.carshering.entity.DocumentType;
import org.example.carshering.mapper.DocumentMapper;
import org.example.carshering.repository.DocumentRepository;
import org.example.carshering.service.ClientService;
import org.example.carshering.service.DocumentService;
import org.example.carshering.service.DocumentTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypeService documentTypeService;
    private final DocumentMapper documentMapper;
    private final ClientService clientService;

    @Override
    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest request, Long userId) {

        if (documentRepository.existsByClientId(userId)) {
            throw new RuntimeException("Документ уже существует");
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
        return documentRepository.existsByClientId(userId);
    }

    @Override
    public DocumentResponse findDocument(Long userId) {
        Document doc = documentRepository.findByClientId(userId)
                .orElseThrow(() -> new RuntimeException("Document not found for user " + clientService.getEntity(userId).getFirstName()));

        return documentMapper.toDto(doc);
    }

    @Override
    public void verifyDocument(Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        doc.setVerified(true);
        documentRepository.save(doc);
    }

    @Override
    public List<DocumentResponse> getAllDocuments(boolean onlyUnverified) {
        List<Document> documents = onlyUnverified
                ? documentRepository.findByVerifiedIsFalse()
                : documentRepository.findAll();

        return documents
                .stream()
                .map(documentMapper::toDto)
                .toList();
    }
}
