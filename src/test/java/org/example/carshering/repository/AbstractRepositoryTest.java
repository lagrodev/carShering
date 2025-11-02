package org.example.carshering.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractRepositoryTest {

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

