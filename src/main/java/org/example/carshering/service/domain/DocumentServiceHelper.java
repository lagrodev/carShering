package org.example.carshering.service.domain;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.DocumentResponse;
import org.example.carshering.mapper.DocumentMapper;
import org.example.carshering.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentServiceHelper {

    public boolean hasDocument(Long userId) {
        return documentRepository.existsByClientIdAndDeletedFalse(userId);
    }
    private final DocumentMapper documentMapper;
    private final DocumentRepository documentRepository;

    public DocumentResponse findDocument(Long userId) {
        return documentRepository.findByClientIdAndDeletedFalse(userId)
                .map(documentMapper::toDto)
                .orElse(null);
    }
}
