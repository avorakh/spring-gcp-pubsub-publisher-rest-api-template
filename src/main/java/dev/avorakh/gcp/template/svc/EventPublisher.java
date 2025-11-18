package dev.avorakh.gcp.template.svc;

import dev.avorakh.gcp.template.model.PublishedEventDto;
import dev.avorakh.gcp.template.model.RequestEventDto;

import java.util.concurrent.CompletableFuture;

public interface EventPublisher {
    CompletableFuture<PublishedEventDto> publishEvent(RequestEventDto event);
}
