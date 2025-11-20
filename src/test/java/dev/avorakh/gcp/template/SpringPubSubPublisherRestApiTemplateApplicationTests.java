package dev.avorakh.gcp.template;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.spring.core.GcpProjectIdProvider;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.gcloud.PubSubEmulatorContainer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.avorakh.gcp.template.test.PostgreSQLContainerUtil;
import dev.avorakh.gcp.template.test.PubSubEmulatorContainerUtil;

@SpringBootTest
class SpringPubSubPublisherRestApiTemplateApplicationTests {


    private static final PostgreSQLContainer<?> POSTGRES = PostgreSQLContainerUtil.createContainer();
    private static final PubSubEmulatorContainer PUBSUB_EMULATOR =PubSubEmulatorContainerUtil.createContainer();

    @BeforeAll
    static void beforeAll() {
        POSTGRES.start();
        PUBSUB_EMULATOR.start();
    }

    @AfterAll
    static void afterAll() {
        POSTGRES.stop();
        PUBSUB_EMULATOR.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainerUtil.configureProperties(registry, POSTGRES);
        PostgreSQLContainerUtil.configureDDLGenerationProperties(registry, "none");
        PubSubEmulatorContainerUtil.configureProperties(registry, PUBSUB_EMULATOR);
    }


    @Autowired
    private CredentialsProvider googleCredentials;

    @Autowired
    private GcpProjectIdProvider gcpProjectIdProvider;

    @Test
    void contextLoads() {
        // context load check
        assertNotNull(googleCredentials, "googleCredentials bean should be present");
        assertNotNull(gcpProjectIdProvider, "gcpProjectIdProvider bean should be present");
    }

}
