package org.example.carshering.it;

import org.example.carshering.security.JwtRequestFilter;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseWebIntegrateTest {
    @MockitoBean
    protected JwtRequestFilter jwtRequestFilter;

    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("carshering")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    private static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeAll
    static void init() {
        if (!postgreSQLContainer.isRunning()) {
            postgreSQLContainer.start();
        }
    }



}