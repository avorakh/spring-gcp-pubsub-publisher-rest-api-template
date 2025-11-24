package dev.avorakh.gcp.template.svc;

import dev.avorakh.gcp.template.entity.EventMessage;
import dev.avorakh.gcp.template.model.PublishedEventDto;
import dev.avorakh.gcp.template.model.RequestEventDto;
import dev.avorakh.gcp.template.repository.EventMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublishedEventPostProcessorImpl Tests")
class PublishedEventPostProcessorImplTest {

    @Mock
    private EventMessageRepository eventMessageRepository;

    @InjectMocks
    private PublishedEventPostProcessorImpl sut;

    private RequestEventDto requestEventDto;
    private PublishedEventDto publishedEventDto;
    private String testEventId;
    private String testEventType;
    private String testPayload;
    private String testMessageId;

    @BeforeEach
    void setUp() {
        testEventId = UUID.randomUUID().toString();
        testEventType = "TEST_EVENT_TYPE";
        testPayload = """
                {
                    "key":"value"
                }
                """;
        testMessageId = "test-message-id-123";

        requestEventDto = new RequestEventDto(testEventId, testEventType, testPayload);
        publishedEventDto = PublishedEventDto.builder()
                .success(true)
                .messageId(testMessageId)
                .build();
    }


    @Test
    @DisplayName("Should process event successfully and save to repository")
    void shouldProcessEventSuccessfully() {
        ArgumentCaptor<EventMessage> eventMessageCaptor = ArgumentCaptor.forClass(EventMessage.class);
        when(eventMessageRepository.save(any(EventMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sut.process(requestEventDto, publishedEventDto);

        verify(eventMessageRepository).save(eventMessageCaptor.capture());
        EventMessage capturedEventMessage = eventMessageCaptor.getValue();

        assertThat(capturedEventMessage).isNotNull();
        assertThat(capturedEventMessage.getId()).isEqualTo(UUID.fromString(testEventId));
        assertThat(capturedEventMessage.getEventType()).isEqualTo(testEventType);
        assertThat(capturedEventMessage.getMessageId()).isEqualTo(testMessageId);
        assertThat(capturedEventMessage.getCreated()).isNotNull();
        assertThat(capturedEventMessage.getModified()).isNotNull();
        assertThat(capturedEventMessage.getCreated()).isEqualTo(capturedEventMessage.getModified());

        Map<String, Object> eventData = capturedEventMessage.getEventData();
        assertThat(eventData).isNotNull();
        assertThat(eventData).hasSize(3);
        assertThat(eventData.get("id")).isEqualTo(testEventId);
        assertThat(eventData.get("type")).isEqualTo(testEventType);
        assertThat(eventData.get("payload")).isEqualTo(testPayload);
    }

    @Test
    @DisplayName("Should return the same PublishedEventDto that was passed in")
    void shouldReturnSamePublishedEventDto() {

        PublishedEventDto inputDto = PublishedEventDto.builder()
                .success(false)
                .messageId("error-message-id")
                .errorReason("Some error occurred")
                .build();

        when(eventMessageRepository.save(any(EventMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PublishedEventDto result = sut.process(requestEventDto, inputDto);

        assertThat(result).isSameAs(inputDto);
        assertThat(result.success()).isFalse();
        assertThat(result.messageId()).isEqualTo("error-message-id");
        assertThat(result.errorReason()).isEqualTo("Some error occurred");

        verify(eventMessageRepository).save(any(EventMessage.class));
    }
}

