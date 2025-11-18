package dev.avorakh.gcp.template.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import com.google.pubsub.v1.PubsubMessage;
import dev.avorakh.gcp.template.model.RequestEventDto;
import dev.avorakh.gcp.template.model.PublishedEventDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.google.protobuf.ByteString.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventPublisherImpl implements EventPublisher {

    PubSubPublisherTemplate pubSubPublisherTemplate;
    ObjectMapper objectMapper;
    @NonFinal
    @Value("${app.pubsub.topic}")
    String topic;


    public CompletableFuture<PublishedEventDto> publishEvent(RequestEventDto event) {

        PubsubMessage message = toPubsubMessage(event);

        return pubSubPublisherTemplate.publish(topic, message)
                .handle((messageId, throwable) -> handleMessage(messageId, throwable, message));
    }

    private PublishedEventDto handleMessage(String messageId, Throwable throwable, PubsubMessage message) {
        var responseBuilder = PublishedEventDto.builder();
        if (throwable != null) {
            if (throwable instanceof CompletionException exception && throwable.getCause() != null) {
                throwable = exception;
            }
            log.error("Unable to publish Pub/Sub message due to error. message:[{}], error:[{}].", message, throwable, throwable);
            responseBuilder.errorReason(throwable.getMessage());
        } else {
            log.info("Pub/Sub message published successfully.message:[{}], messageId:[{}]", message, messageId);
            responseBuilder
                    .messageId(messageId)
                    .success(true);
        }
        return responseBuilder.build();
    }

    private PubsubMessage toPubsubMessage(RequestEventDto event) {
        byte[] jsonMessageBytes = toJsonBytes(event);

        return PubsubMessage.newBuilder()
                .setData(copyFrom(jsonMessageBytes))
                .putAttributes("eventId", event.getId())
                .putAttributes("eventType", event.getType())
                .build();
    }

    private byte[] toJsonBytes(RequestEventDto event) {
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (JsonProcessingException e) {
            log.error("Unable write event.", e);
            throw new RuntimeException(e);
        }
    }
}
