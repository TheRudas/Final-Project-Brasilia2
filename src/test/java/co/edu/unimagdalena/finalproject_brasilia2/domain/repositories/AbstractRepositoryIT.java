package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for repositories' tests.
 * - @DataJpaTest: levanta solo la capa JPA (rápido)
 * - @Testcontainers + @ServiceConnection: autoconfigura el DataSource con Postgres 16 en contenedor
 */

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractRepositoryIT {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    //  Extension point if I need common helpers.
}
