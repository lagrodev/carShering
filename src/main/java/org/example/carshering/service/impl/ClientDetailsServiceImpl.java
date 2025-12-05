package org.example.carshering.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.domain.entity.Client;
import org.example.carshering.repository.ClientRepository;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.interfaces.ClientDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientDetailsServiceImpl implements ClientDetailsService {

    private final ClientRepository clientRepository;

    // todo добавить кэширование?? (если будет медленно работать)
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


    @Override
    public void setEmailVerified(Client client) {
        client.setEmailVerified(true);

        clientRepository.save(client);
    }


}