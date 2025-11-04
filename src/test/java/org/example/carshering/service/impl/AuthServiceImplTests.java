package org.example.carshering.service.impl;

import org.example.carshering.dto.request.JwtRequest;
import org.example.carshering.security.ClientDetails;
import org.example.carshering.service.ClientDetailsService;
import org.example.carshering.utils.JwtTokenUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTests {

    @Mock
    private JwtTokenUtils jwtTokenUtils;

    @Mock
    private ClientDetailsService clientDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl serviceUnderTest;

    @Test
    @DisplayName("Test createAuthToken returns JWT when credentials are valid")
    public void givenValidCredentials_whenCreateAuthToken_thenReturnToken() {
        // given
        JwtRequest request = new JwtRequest("john", "password");

        ClientDetails userDetails = mock(ClientDetails.class);
        given(clientDetailsService.loadUserByUsername("john"))
                .willReturn(userDetails);
        given(jwtTokenUtils.generateToken(userDetails))
                .willReturn("jwt-token");

        // when
        String result = serviceUnderTest.createAuthToken(request);

        // then
        assertThat(result).isEqualTo("jwt-token");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());

        UsernamePasswordAuthenticationToken token = captor.getValue();
        assertThat(token.getName()).isEqualTo("john");
        assertThat(token.getCredentials()).isEqualTo("password");

        verify(clientDetailsService).loadUserByUsername("john");
        verify(jwtTokenUtils).generateToken(userDetails);
    }

    @Test
    @DisplayName("Test createAuthToken throws BadCredentialsException when credentials invalid")
    public void givenInvalidCredentials_whenCreateAuthToken_thenThrowBadCredentialsException() {
        // given
        JwtRequest request = new JwtRequest("john", "wrongPassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(new UsernamePasswordAuthenticationToken("john", "wrongPassword"));

        // when + then
        assertThrows(
                BadCredentialsException.class,
                () -> serviceUnderTest.createAuthToken(request)
        );

        verify(authenticationManager)
                .authenticate(new UsernamePasswordAuthenticationToken("john", "wrongPassword"));
        verifyNoInteractions(clientDetailsService, jwtTokenUtils);
    }

    @Test
    @DisplayName("Test createAuthToken throws LockedException when account is locked")
    public void givenLockedAccount_whenCreateAuthToken_thenThrowLockedException() {
        // given
        JwtRequest request = new JwtRequest("john", "password");

        doThrow(new LockedException("Account locked"))
                .when(authenticationManager)
                .authenticate(new UsernamePasswordAuthenticationToken("john", "password"));

        // when + then
        assertThrows(
                LockedException.class,
                () -> serviceUnderTest.createAuthToken(request)
        );

        verify(authenticationManager)
                .authenticate(new UsernamePasswordAuthenticationToken("john", "password"));
        verifyNoInteractions(clientDetailsService, jwtTokenUtils);
    }

    @Test
    @DisplayName("Test createAuthToken calls dependencies in correct order")
    public void givenValidRequest_whenCreateAuthToken_thenVerifyInvocationOrder() {
        // given
        JwtRequest request = new JwtRequest("alice", "qwerty");

        ClientDetails userDetails = mock(ClientDetails.class);
        given(clientDetailsService.loadUserByUsername("alice"))
                .willReturn(userDetails);
        given(jwtTokenUtils.generateToken(userDetails))
                .willReturn("valid-token");

        // when
        serviceUnderTest.createAuthToken(request);

        // then
        InOrder inOrder = inOrder(authenticationManager, clientDetailsService, jwtTokenUtils);
        inOrder.verify(authenticationManager)
                .authenticate(new UsernamePasswordAuthenticationToken("alice", "qwerty"));
        inOrder.verify(clientDetailsService).loadUserByUsername("alice");
        inOrder.verify(jwtTokenUtils).generateToken(userDetails);
    }

//

    @Test
    @DisplayName("Test createAuthToken throws BadCredentialsException with correct message when credentials invalid")
    public void givenInvalidCredentials_whenCreateAuthToken_thenThrowBadCredentialsExceptionWithMessage() {
        // given
        JwtRequest request = new JwtRequest("john", "wrongPassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // when + then
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> serviceUnderTest.createAuthToken(request)
        );

        assertThat(exception.getMessage()).isEqualTo("Неправильный логин или пароль");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());

        UsernamePasswordAuthenticationToken token = captor.getValue();
        assertThat(token.getName()).isEqualTo("john");
        assertThat(token.getCredentials()).isEqualTo("wrongPassword");

        verifyNoInteractions(clientDetailsService, jwtTokenUtils);
    }

    @Test
    @DisplayName("Test createAuthToken throws LockedException with correct message when account is locked")
    public void givenLockedAccount_whenCreateAuthToken_thenThrowLockedExceptionWithMessage() {
        // given
        JwtRequest request = new JwtRequest("john", "password");

        doThrow(new LockedException("Account locked"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // when + then
        LockedException exception = assertThrows(
                LockedException.class,
                () -> serviceUnderTest.createAuthToken(request)
        );

        assertThat(exception.getMessage()).isEqualTo("Аккаунт заблокирован");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());

        UsernamePasswordAuthenticationToken token = captor.getValue();
        assertThat(token.getName()).isEqualTo("john");
        assertThat(token.getCredentials()).isEqualTo("password");

        verifyNoInteractions(clientDetailsService, jwtTokenUtils);
    }



    @Test
    @DisplayName("Test createAuthToken throws UsernameNotFoundException when user not found")
    public void givenNonExistentUser_whenCreateAuthToken_thenThrowUsernameNotFoundException() {
        // given
        JwtRequest request = new JwtRequest("unknown", "password");

        given(clientDetailsService.loadUserByUsername("unknown"))
                .willThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        // when + then
        assertThrows(
                org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> serviceUnderTest.createAuthToken(request)
        );

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("unknown", "password"));
        verify(clientDetailsService).loadUserByUsername("unknown");
        verifyNoInteractions(jwtTokenUtils);
    }

}
