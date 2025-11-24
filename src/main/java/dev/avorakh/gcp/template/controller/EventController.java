package dev.avorakh.gcp.template.controller;

import dev.avorakh.gcp.template.model.RequestEventDto;
import dev.avorakh.gcp.template.model.PublishedEventDto;
import dev.avorakh.gcp.template.svc.EventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventController {

    EventPublisher publisher;


    @PostMapping
    public CompletableFuture<ResponseEntity<PublishedEventDto>> publish(@RequestBody RequestEventDto event) {
        if (event.getId() == null || event.getId().isEmpty()) {
            event.setId(UUID.randomUUID().toString());
        }
        return publisher.publishEvent(event)
                .thenApply(ResponseEntity::ok);
    }
}

