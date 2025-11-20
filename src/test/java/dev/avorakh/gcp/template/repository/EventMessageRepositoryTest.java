package dev.avorakh.gcp.template.repository;

import dev.avorakh.gcp.template.entity.EventMessage;
import dev.avorakh.gcp.template.test.PostgreSQLContainerUtil;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("EventMessageRepository Tests")
class EventMessageRepositoryTest {

    private static final PostgreSQLContainer<?> postgres = PostgreSQLContainerUtil.createContainer();

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainerUtil.configureProperties(registry, postgres);
        PostgreSQLContainerUtil.configureDDLGenerationProperties(registry, "create-drop");
    }

    private EventMessage testEventMessage;
    private UUID testId;
    private Map<String, Object> eventData;

    @Autowired
    private EventMessageRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        testId = UUID.randomUUID();
        eventData = toTestEventData();
        testEventMessage = EventMessage.builder()
                .id(testId)
                .eventType("TEST_EVENT")
                .eventData(eventData)
                .messageId("msg-123")
                .created("2024-01-01T00:00:00Z")
                .modified("2024-01-01T00:00:00Z")
                .build();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @Test
    @DisplayName("Should save event message successfully")
    void shouldSaveEventMessage() {

        EventMessage actual = repository.save(testEventMessage);

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(testId);
        assertThat(actual.getEventType()).isEqualTo(testEventMessage.getEventType());
        assertThat(actual.getMessageId()).isEqualTo(testEventMessage.getMessageId());
        assertThat(actual.getEventData()).isNotNull();
        assertThat(actual.getEventData()).isEqualTo(eventData);
    }

    @Test
    @DisplayName("Should find event message by ID")
    void shouldFindEventMessageById() {
        repository.save(testEventMessage);

        Optional<EventMessage> found = repository.findById(testId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testId);
    }

    @Test
    @DisplayName("Should return empty when event message not found")
    void shouldReturnEmptyWhenNotFound() {
        Optional<EventMessage> found = repository.findById(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all event messages")
    void shouldFindAllEventMessages() {
        var message1 = EventMessage.builder()
                .id(UUID.randomUUID())
                .eventType("EVENT_TYPE_1")
                .eventData(Map.of("data", "value1"))
                .messageId("msg-1")
                .created("2024-01-01T00:00:00Z")
                .build();

        var message2 = EventMessage.builder()
                .id(UUID.randomUUID())
                .eventType("EVENT_TYPE_2")
                .eventData(Map.of("data", "value2"))
                .messageId("msg-2")
                .created("2024-01-02T00:00:00Z")
                .build();

        repository.save(message1);
        repository.save(message2);

        List<EventMessage> actualMessages = repository.findAll();

        assertThat(actualMessages).hasSize(2);
        assertThat(actualMessages).extracting(EventMessage :: getEventType)
                .containsExactlyInAnyOrder("EVENT_TYPE_1", "EVENT_TYPE_2");
    }

    @Test
    @DisplayName("Should update event message")
    void shouldUpdateEventMessage() {

        EventMessage saved = repository.save(testEventMessage);
        String newEventType = "UPDATED_EVENT";
        Map<String, Object> updatedEventData = new HashMap<>();
        updatedEventData.put("newKey", "newValue");


        saved.setEventType(newEventType);
        saved.setEventData(updatedEventData);
        saved.setModified("2024-01-02T00:00:00Z");
        EventMessage actualUpdated = repository.save(saved);

        assertThat(actualUpdated.getEventType()).isEqualTo(newEventType);
        assertThat(actualUpdated.getEventData().get("newKey")).isEqualTo("newValue");
        assertThat(actualUpdated.getModified()).isEqualTo("2024-01-02T00:00:00Z");
    }

    @Test
    @DisplayName("Should delete event message by ID")
    void shouldDeleteEventMessageById() {
        repository.save(testEventMessage);
        assertThat(repository.findById(testId)).isPresent();

        repository.deleteById(testId);

        assertThat(repository.findById(testId)).isEmpty();
    }

    @Test
    @DisplayName("Should delete event message entity")
    void shouldDeleteEventMessageEntity() {
        EventMessage message = repository.save(testEventMessage);
        assertThat(repository.findById(testId)).isPresent();

        repository.delete(message);

        assertThat(repository.findById(testId)).isEmpty();
    }

    @Test
    @DisplayName("Should save event message with null optional fields")
    void shouldSaveEventMessageWithNullOptionalFields() {

        var messageWithNulls = EventMessage.builder()
                .id(UUID.randomUUID())
                .eventType("REQUIRED_EVENT")
                .build();

        EventMessage actual = repository.save(messageWithNulls);

        assertThat(actual).isNotNull();
        assertThat(actual.getEventType()).isEqualTo("REQUIRED_EVENT");
        assertThat(actual.getEventData()).isNull();
        assertThat(actual.getMessageId()).isNull();
    }

    @Test
    @DisplayName("Should save event message with complex JSONB data")
    void shouldSaveEventMessageWithComplexJsonbData() {

        Map<String, Object> complexData = new HashMap<>();
        complexData.put("stringField", "test");
        complexData.put("numberField", 42);
        complexData.put("booleanField", true);
        complexData.put("nestedObject", Map.of("nestedKey", "nestedValue"));
        complexData.put("arrayField", List.of("item1", "item2", "item3"));

        var message = EventMessage.builder()
                .id(UUID.randomUUID())
                .eventType("COMPLEX_EVENT")
                .eventData(complexData)
                .messageId("complex-msg")
                .created("2024-01-01T00:00:00Z")
                .build();

        EventMessage actualSaved = repository.save(message);

        EventMessage actualFound = repository.findById(actualSaved.getId()).orElseThrow();

        assertThat(actualFound.getEventData()).isNotNull();
        assertThat(actualFound.getEventData().get("stringField")).isEqualTo("test");
        assertThat(actualFound.getEventData().get("numberField")).isEqualTo(42);
        assertThat(actualFound.getEventData().get("booleanField")).isEqualTo(true);
        assertThat(actualFound.getEventData().get("nestedObject")).isNotNull();
        assertThat(actualFound.getEventData().get("arrayField")).isNotNull();
    }

    @Test
    @DisplayName("Should count event messages")
    void shouldCountEventMessages() {

        assertThat(repository.count()).isEqualTo(0);

        repository.save(testEventMessage);
        repository.save(EventMessage.builder()
                                .id(UUID.randomUUID())
                                .eventType("ANOTHER_EVENT")
                                .eventData(Map.of("data", "value"))
                                .messageId("msg-2")
                                .created("2024-01-01T00:00:00Z")
                                .build());

        long count = repository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should check if event message exists by ID")
    void shouldCheckIfEventMessageExistsById() {
        assertThat(repository.existsById(testId)).isFalse();

        repository.save(testEventMessage);

        boolean exists = repository.existsById(testId);

        assertThat(exists).isTrue();
    }

    private static @NotNull Map<String, Object> toTestEventData() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("key1", "value1");
        eventData.put("key2", 123);
        eventData.put("key3", true);
        return eventData;
    }
}

