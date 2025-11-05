package org.example.carshering.rest;

import org.example.carshering.security.JwtRequestFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseWebMvcTest {
    @MockitoBean
    protected JwtRequestFilter jwtRequestFilter;
}