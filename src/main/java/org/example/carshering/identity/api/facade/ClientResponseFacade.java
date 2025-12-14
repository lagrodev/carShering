package org.example.carshering.identity.api.facade;

import lombok.RequiredArgsConstructor;
import org.example.carshering.identity.api.dto.response.AllUserResponse;
import org.example.carshering.identity.api.dto.response.DocumentResponse;
import org.example.carshering.identity.api.dto.response.ShortUserResponse;
import org.example.carshering.identity.api.dto.response.UserResponse;
import org.example.carshering.identity.api.mapper.ClientMapper;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.api.mapper.DocumentMapper;
import org.example.carshering.identity.application.dto.response.DocumentDto;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientResponseFacade {
    private final ClientMapper clientMapper;
    private final DocumentMapper documentMapper;

    public UserResponse getClientById(ClientDto clientDto) {
        return clientMapper.toDto(clientDto);
    }

    public ShortUserResponse getShortUserById(ClientDto clientDto) {
        return clientMapper.toShortDtoForAdmin(clientDto);
    }

    public AllUserResponse getAllUserResponse(ClientDto clientDto) {
        return clientMapper.toDtoForAdmin(clientDto);
    }

    public DocumentResponse getDocumentResponse(DocumentDto documentDto) {
        return documentMapper.toDto(documentDto);
    }



}
