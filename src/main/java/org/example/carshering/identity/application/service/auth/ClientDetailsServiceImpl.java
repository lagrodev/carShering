package org.example.carshering.identity.application.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.identity.application.dto.response.ClientDto;
import org.example.carshering.identity.application.dto.response.RoleDto;
import org.example.carshering.identity.application.service.ClientApplicationService;
import org.example.carshering.security.ClientDetails;
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

    private final ClientApplicationService clientRepository;


    // todo добавить кэширование?? (если будет медленно работать)
    @Override
    @Transactional
    public ClientDetails loadUserByUsername(String username) {


        ClientDto client = clientRepository.findUserByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Пользователь '%s' не найден", username)));


        RoleDto role = clientRepository.findRoleByRoleId(client.roleId())
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Роль с id '%d' не найдена", client.roleId())));
        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        return new ClientDetails(
                client.id(),
                client.login(),
                client.password(),
                client.email(),
                authorities,
                client.banned(),
                client.deleted()
        );
    }


}