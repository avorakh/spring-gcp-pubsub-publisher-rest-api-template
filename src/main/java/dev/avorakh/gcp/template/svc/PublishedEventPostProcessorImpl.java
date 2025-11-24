package dev.avorakh.gcp.template.svc;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;

import java.util.Map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;

import dev.avorakh.gcp.template.entity.EventMessage;
import dev.avorakh.gcp.template.model.PublishedEventDto;
import dev.avorakh.gcp.template.model.RequestEventDto;
import dev.avorakh.gcp.template.repository.EventMessageRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishedEventPostProcessorImpl implements PublishedEventPostProcessor{

    EventMessageRepository eventMessageRepository;

    @Override
    public PublishedEventDto process(RequestEventDto event, PublishedEventDto publishedEventDto) {

        EventMessage eventMessage = toEventMessage(event, publishedEventDto);

        eventMessageRepository.save(eventMessage);

        return publishedEventDto;

    }


    private EventMessage toEventMessage(RequestEventDto event, PublishedEventDto publishedEventDto) {
        String now = now(UTC).toString();

        var eventData = Map.<String, Object>of(
                "id", event.getId(),
                "type", event.getType(),
                "payload", event.getPayload()
        );

        return EventMessage.builder()
                .id(java.util.UUID.fromString(event.getId()))
                .eventType(event.getType())
                .eventData(eventData)
                .messageId(publishedEventDto.messageId())
                .created(now)
                .modified(now)
                .build();
    }
}
