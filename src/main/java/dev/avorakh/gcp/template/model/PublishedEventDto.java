package dev.avorakh.gcp.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PublishedEventDto(boolean success, String messageId, String errorReason) {
}
