package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.carshering.entity.Client;
import org.example.carshering.repository.ClientRepository;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.ClientDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientDetailsServiceImpl implements ClientDetailsService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public ClientDetails loadUserByUsername(String username) {

        System.out.println("loadUserByUsername");

        Client client = clientRepository.getClientByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Пользователь '%s' не найден", username)));

        System.out.println("client" + client);

        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + client.getRole().getName()));
        return new ClientDetails(
                client.getId(),
                client.getLogin(),
                client.getPassword(),
                client.getEmail(),
                authorities,
                client.isBanned(),
                client.isDeleted()
        );
    }
}