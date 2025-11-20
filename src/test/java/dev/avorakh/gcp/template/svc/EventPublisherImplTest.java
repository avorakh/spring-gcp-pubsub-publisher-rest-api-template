package dev.avorakh.gcp.template.svc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;

import dev.avorakh.gcp.template.test.PubSubEmulatorContainerUtil;
import dev.avorakh.gcp.template.model.PublishedEventDto;
import dev.avorakh.gcp.template.model.RequestEventDto;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.gcloud.PubSubEmulatorContainer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static dev.avorakh.gcp.template.test.PubSubEmulatorContainerUtil.PUBSUB_EMULATOR_HOST;
import static dev.avorakh.gcp.template.test.PubSubEmulatorContainerUtil.TOPIC_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("EventPublisherImpl Tests")
class EventPublisherImplTest {


    private static final PubSubEmulatorContainer PUBSUB_EMULATOR = PubSubEmulatorContainerUtil.createContainer();

    @Mock
    private PublishedEventPostProcessor publishedEventPostProcessor;

    private EventPublisherImpl eventPublisher;
    private PubSubPublisherTemplate pubSubPublisherTemplate;
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll() throws IOException {
        PUBSUB_EMULATOR.start();
        String emulatorEndpoint = PUBSUB_EMULATOR.getEmulatorEndpoint();
        System.setProperty(PUBSUB_EMULATOR_HOST, emulatorEndpoint);
        PubSubEmulatorContainerUtil.createTopic(PUBSUB_EMULATOR);
    }

    @AfterAll
    static void afterAll() {
        PUBSUB_EMULATOR.stop();
        System.clearProperty(PUBSUB_EMULATOR_HOST);
    }

    @BeforeEach
    void setUp() throws IOException {
        pubSubPublisherTemplate = PubSubEmulatorContainerUtil.createPubSubPublisherTemplate(PUBSUB_EMULATOR);
        objectMapper = new ObjectMapper();

        eventPublisher = new EventPublisherImpl(
                pubSubPublisherTemplate,
                objectMapper,
                publishedEventPostProcessor
        );

        eventPublisher.setTopic(TOPIC_NAME);
    }

    @Test
    @DisplayName("Should publish event successfully")
    void shouldPublishEventSuccessfully() throws ExecutionException, InterruptedException, TimeoutException {

        String eventId = "test-event-id-123";
        String eventType = "TEST_EVENT_TYPE";
        String payload = "{\"key\":\"value\"}";
        var requestEvent = new RequestEventDto(eventId, eventType, payload);

        var  processedEventDto = PublishedEventDto.builder()
                .success(true)
                .messageId("test-message-id")
                .build();

        when(publishedEventPostProcessor.process(eq(requestEvent), any(PublishedEventDto.class)))
                .thenReturn(processedEventDto);

        CompletableFuture<PublishedEventDto> future = eventPublisher.publishEvent(requestEvent);
        PublishedEventDto result = future.get(10, TimeUnit.SECONDS);

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.messageId()).isNotNull();
        assertThat(result.errorReason()).isNull();

        verify(publishedEventPostProcessor).process(eq(requestEvent), any(PublishedEventDto.class));
    }

    @Test
    @DisplayName("Should handle publish failure and return error in PublishedEventDto")
    void shouldHandlePublishFailure() throws ExecutionException, InterruptedException, TimeoutException {

        String eventId = "test-event-id-456";
        String eventType = "TEST_EVENT_TYPE";
        String payload = "{\"key\":\"value\"}";
        var requestEvent = new RequestEventDto(eventId, eventType, payload);

        eventPublisher.setTopic("non-existent-topic");

        var processedEventDto = PublishedEventDto.builder()
                .success(false)
                .errorReason("Topic not found")
                .build();

        when(publishedEventPostProcessor.process(eq(requestEvent), any(PublishedEventDto.class)))
                .thenReturn(processedEventDto);


        CompletableFuture<PublishedEventDto> future = eventPublisher.publishEvent(requestEvent);
        PublishedEventDto result = future.get(10, TimeUnit.SECONDS);

        assertThat(result).isNotNull();
        assertThat(result.success()).isFalse();
        assertThat(result.errorReason()).isNotNull();

        verify(publishedEventPostProcessor).process(eq(requestEvent), any(PublishedEventDto.class));
    }

    @Test
    @DisplayName("Should correctly serialize event to PubSub message")
    void shouldCorrectlySerializeEventToPubSubMessage() throws ExecutionException, InterruptedException, TimeoutException {

        String eventId = "test-event-id-789";
        String eventType = "SERIALIZATION_TEST";
        String payload = "{\"testKey\":\"testValue\",\"number\":42}";
        var requestEvent = new RequestEventDto(eventId, eventType, payload);

        var processedEventDto = PublishedEventDto.builder()
                .success(true)
                .messageId("serialized-message-id")
                .build();

        when(publishedEventPostProcessor.process(eq(requestEvent), any(PublishedEventDto.class)))
                .thenReturn(processedEventDto);


        CompletableFuture<PublishedEventDto> future = eventPublisher.publishEvent(requestEvent);
        PublishedEventDto result = future.get(10, TimeUnit.SECONDS);

        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.messageId()).isNotNull();


        verify(publishedEventPostProcessor).process(eq(requestEvent), any(PublishedEventDto.class));
    }

    @Test
    @DisplayName("Should process event through post processor")
    void shouldProcessEventThroughPostProcessor() throws ExecutionException, InterruptedException, TimeoutException {

        String eventId = "test-event-id-post-process";
        String eventType = "POST_PROCESS_TEST";
        String payload = "{\"data\":\"test\"}";
        var requestEvent = new RequestEventDto(eventId, eventType, payload);

        var processedEventDto = PublishedEventDto.builder()
                .success(true)
                .messageId("processed-message-id")
                .build();

        when(publishedEventPostProcessor.process(eq(requestEvent), any(PublishedEventDto.class)))
                .thenReturn(processedEventDto);


        CompletableFuture<PublishedEventDto> future = eventPublisher.publishEvent(requestEvent);
        PublishedEventDto result = future.get(10, TimeUnit.SECONDS);

        assertThat(result).isNotNull();
        assertThat(result.messageId()).isEqualTo("processed-message-id");
        assertThat(result.success()).isTrue();

        verify(publishedEventPostProcessor).process(eq(requestEvent), any(PublishedEventDto.class));
    }
}

