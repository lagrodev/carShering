package org.example.carshering.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.service.ClientDetailsService;
import org.example.carshering.service.impl.ClientDetailsServiceImpl;
import org.example.carshering.utils.JwtTokenUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final ClientDetailsService clientDetailsService;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
            try {

                username = jwtTokenUtils.getUsernameFromToken(jwtToken);


                ClientDetails userDetails = (ClientDetails) clientDetailsService.loadUserByUsername(username);

                if (!userDetails.isAccountNonLocked()) {
                    log.warn("Blocked user {} tried to access", username);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Account not found");
                    return;
                }

            } catch (ExpiredJwtException e) {
                log.debug("Время токена вышло");
            } catch (SignatureException e) {
                log.debug("Подпись неправильная");
            } catch (Exception e) {
                log.debug("Невалидный токен");
            }

        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<SimpleGrantedAuthority> authorities = jwtTokenUtils.getAuthoritiesFromToken(jwtToken);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    clientDetailsService.loadUserByUsername(username),
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(token);

        }
        filterChain.doFilter(request, response);
    }
}
