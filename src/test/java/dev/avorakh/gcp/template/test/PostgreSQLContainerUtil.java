package dev.avorakh.gcp.template.test;

import lombok.experimental.UtilityClass;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

@UtilityClass
public class PostgreSQLContainerUtil {


    public static final String TEST_DATABASE_NAME = "testdb";
    public static final String TEST_USERNAME = "test";
    public static final String TEST_PASSWORD = "test";

    private static final String DEFAULT_IMAGE_NAME = "postgres:16-alpine";

    public PostgreSQLContainer<?> createContainer(String imageName) {
        return new PostgreSQLContainer<>(imageName)
                .withDatabaseName(TEST_DATABASE_NAME)
                .withUsername(TEST_USERNAME)
                .withPassword(TEST_PASSWORD);
    }

    public PostgreSQLContainer<?> createContainer() {
        return createContainer(DEFAULT_IMAGE_NAME);
    }

    public void configureProperties(DynamicPropertyRegistry registry, PostgreSQLContainer<?> container) {
        registry.add("spring.datasource.url", container :: getJdbcUrl);
        registry.add("spring.datasource.username", container :: getUsername);
        registry.add("spring.datasource.password", container :: getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    public void configureDDLGenerationProperties(DynamicPropertyRegistry registry, String ddlAuto) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> ddlAuto);
    }
}
