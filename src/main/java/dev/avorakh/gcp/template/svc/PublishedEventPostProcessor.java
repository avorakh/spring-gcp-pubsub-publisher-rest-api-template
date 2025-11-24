package dev.avorakh.gcp.template.svc;

import dev.avorakh.gcp.template.model.PublishedEventDto;
import dev.avorakh.gcp.template.model.RequestEventDto;

public interface PublishedEventPostProcessor {

    PublishedEventDto process(RequestEventDto event, PublishedEventDto publishedEventDto);
}
