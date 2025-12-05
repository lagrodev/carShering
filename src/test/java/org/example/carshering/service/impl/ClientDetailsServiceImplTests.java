package org.example.carshering.service.impl;

import org.example.carshering.domain.entity.Client;
import org.example.carshering.domain.entity.Role;
import org.example.carshering.repository.ClientRepository;
import org.example.carshering.security.ClientDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientDetailsServiceImplTests {
    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientDetailsServiceImpl serviceUnderTest;


    @Test
    @DisplayName("Test loadUserByUsername returns ClientDetails when client exists")
    public void givenExistingUsername_whenLoadUserByUsername_thenReturnClientDetails() {
        // given
        Role role = new Role();
        role.setName("USER");

        Client client = new Client();
        client.setId(1L);
        client.setLogin("john");
        client.setPassword("encodedPassword");
        client.setEmail("john@example.com");
        client.setRole(role);
        client.setBanned(false);
        client.setDeleted(false);

        given(clientRepository.getClientByLogin("john"))
                .willReturn(Optional.of(client));

        // when
        ClientDetails details = serviceUnderTest.loadUserByUsername("john");

        // then
        assertThat(details).isNotNull();
        assertThat(details.getUsername()).isEqualTo("john");
        assertThat(details.getPassword()).isEqualTo("encodedPassword");
        assertThat(details.getEmail()).isEqualTo("john@example.com");
        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        assertThat(details.isBanned()).isFalse();
        assertThat(details.isDeleted()).isFalse();

        verify(clientRepository).getClientByLogin("john");
    }

    @Test
    @DisplayName("Test loadUserByUsername throws exception when user not found")
    public void givenNonExistingUsername_whenLoadUserByUsername_thenThrowUsernameNotFoundException() {
        // given
        given(clientRepository.getClientByLogin("unknown"))
                .willReturn(Optional.empty());

        // when + then
        assertThrows(
                UsernameNotFoundException.class,
                () -> serviceUnderTest.loadUserByUsername("unknown")
        );

        verify(clientRepository).getClientByLogin("unknown");
    }

    @Test
    @DisplayName("Test loadUserByUsername assigns correct ROLE_ prefix")
    public void givenClientWithRoleAdmin_whenLoadUserByUsername_thenAuthorityHasRolePrefix() {
        // given
        Role role = new Role();
        role.setName("ADMIN");

        Client client = new Client();
        client.setId(2L);
        client.setLogin("admin");
        client.setPassword("secret");
        client.setEmail("admin@corp.com");
        client.setRole(role);
        client.setBanned(false);
        client.setDeleted(false);

        given(clientRepository.getClientByLogin("admin"))
                .willReturn(Optional.of(client));

        // when
        ClientDetails details = serviceUnderTest.loadUserByUsername("admin");

        // then
        assertThat(details.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_ADMIN");

        verify(clientRepository).getClientByLogin("admin");
    }

    @Test
    @DisplayName("Test loadUserByUsername sets banned and deleted flags correctly")
    public void givenBannedAndDeletedClient_whenLoadUserByUsername_thenFlagsAreSet() {
        // given
        Role role = new Role();
        role.setName("USER");

        Client client = new Client();
        client.setId(3L);
        client.setLogin("bannedUser");
        client.setPassword("pass");
        client.setEmail("ban@ban.com");
        client.setRole(role);
        client.setBanned(true);
        client.setDeleted(true);

        given(clientRepository.getClientByLogin("bannedUser"))
                .willReturn(Optional.of(client));

        // when
        ClientDetails details = serviceUnderTest.loadUserByUsername("bannedUser");

        // then
        assertThat(details.isBanned()).isTrue();
        assertThat(details.isDeleted()).isTrue();
        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        verify(clientRepository).getClientByLogin("bannedUser");
    }

    @Test
    @DisplayName("Test loadUserByUsername throws exception when client role is null")
    public void givenClientWithNullRole_whenLoadUserByUsername_thenThrowNullPointerException() {
        // given
        Client client = new Client();
        client.setId(4L);
        client.setLogin("noRoleUser");
        client.setPassword("pass");
        client.setEmail("norole@example.com");
        client.setRole(null); // intentional
        client.setBanned(false);
        client.setDeleted(false);

        given(clientRepository.getClientByLogin("noRoleUser"))
                .willReturn(Optional.of(client));

        // when + then
        assertThrows(
                NullPointerException.class,
                () -> serviceUnderTest.loadUserByUsername("noRoleUser")
        );

        verify(clientRepository).getClientByLogin("noRoleUser");
    }

    @Test
    @DisplayName("Test loadUserByUsername throws exception when username is empty")
    public void givenEmptyUsername_whenLoadUserByUsername_thenThrowUsernameNotFoundException() {
        // given
        given(clientRepository.getClientByLogin(""))
                .willReturn(Optional.empty());

        // when + then
        assertThrows(
                UsernameNotFoundException.class,
                () -> serviceUnderTest.loadUserByUsername("")
        );

        verify(clientRepository).getClientByLogin("");
    }

    @Test
    @DisplayName("Test loadUserByUsername throws exception when username is blank")
    public void givenBlankUsername_whenLoadUserByUsername_thenThrowUsernameNotFoundException() {
        // given
        String blankUsername = "   ";
        given(clientRepository.getClientByLogin(blankUsername))
                .willReturn(Optional.empty());

        // when + then
        assertThrows(
                UsernameNotFoundException.class,
                () -> serviceUnderTest.loadUserByUsername(blankUsername)
        );

        verify(clientRepository).getClientByLogin(blankUsername);
    }

}
