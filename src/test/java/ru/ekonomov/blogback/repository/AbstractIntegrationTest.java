package ru.ekonomov.blogback.repository;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.ekonomov.blogback.config.RootConfiguration;

@SpringJUnitConfig(RootConfiguration.class)
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("SPRING_DATASOURCE_URL", POSTGRES::getJdbcUrl);
        r.add("SPRING_DATASOURCE_USERNAME", POSTGRES::getUsername);
        r.add("SPRING_DATASOURCE_PASSWORD", POSTGRES::getPassword);
        r.add("SPRING_DATASOURCE_DRIVER", () -> "org.postgresql.Driver");
    }
}
