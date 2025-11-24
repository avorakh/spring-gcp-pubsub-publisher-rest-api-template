package dev.avorakh.gcp.template.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.avorakh.gcp.template.model.PublishedEventDto;
import dev.avorakh.gcp.template.model.RequestEventDto;
import dev.avorakh.gcp.template.svc.EventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventPublisher publisher;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSuccessfullyPublishEventWithExistingId() throws Exception {

        String eventId = UUID.randomUUID().toString();
        String eventType = "test-event";
        String payload = "{\"key\":\"value\"}";
        String messageId = "message-id-123";
        
        RequestEventDto requestEvent = new RequestEventDto(eventId, eventType, payload);
        PublishedEventDto publishedEvent = PublishedEventDto.builder()
                .success(true)
                .messageId(messageId)
                .build();

        when(publisher.publishEvent(any(RequestEventDto.class)))
                .thenReturn(CompletableFuture.completedFuture(publishedEvent));


        var result = mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestEvent)))
                .andDo(print())
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value(messageId))
                .andExpect(jsonPath("$.errorReason").doesNotExist());
    }

    @Test
    void shouldSuccessfullyPublishEventWithNullId() throws Exception {
        // Given
        String eventType = "test-event";
        String payload = "{\"key\":\"value\"}";
        String messageId = "message-id-456";
        
        RequestEventDto requestEvent = new RequestEventDto(null, eventType, payload);
        PublishedEventDto publishedEvent = PublishedEventDto.builder()
                .success(true)
                .messageId(messageId)
                .build();

        ArgumentCaptor<RequestEventDto> eventCaptor = forClass(RequestEventDto.class);
        when(publisher.publishEvent(any(RequestEventDto.class)))
                .thenReturn(CompletableFuture.completedFuture(publishedEvent));

        var result = mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestEvent)))
                .andDo(print())
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value(messageId));


        verify(publisher).publishEvent(eventCaptor.capture());
        RequestEventDto capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent.getId());
        assertFalse(capturedEvent.getId().isEmpty());
    }

    @Test
    void shouldSuccessfullyPublishEventWithEmptyId() throws Exception {

        String eventType = "test-event";
        String payload = "{\"key\":\"value\"}";
        String messageId = "message-id-789";
        
        RequestEventDto requestEvent = new RequestEventDto("", eventType, payload);
        PublishedEventDto publishedEvent = PublishedEventDto.builder()
                .success(true)
                .messageId(messageId)
                .build();

        ArgumentCaptor<RequestEventDto> eventCaptor = forClass(RequestEventDto.class);
        when(publisher.publishEvent(any(RequestEventDto.class)))
                .thenReturn(CompletableFuture.completedFuture(publishedEvent));

        var result = mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestEvent)))
                .andDo(print())
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value(messageId));


        verify(publisher).publishEvent(eventCaptor.capture());
        RequestEventDto capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent.getId());
        assertFalse(capturedEvent.getId().isEmpty());
    }

    @Test
    void shouldReturnErrorResponseWhenPublishFails() throws Exception {

        String eventId = UUID.randomUUID().toString();
        String eventType = "test-event";
        String payload = "{\"key\":\"value\"}";
        String errorReason = "Publish failed";
        
        RequestEventDto requestEvent = new RequestEventDto(eventId, eventType, payload);
        PublishedEventDto publishedEvent = PublishedEventDto.builder()
                .success(false)
                .errorReason(errorReason)
                .build();

        when(publisher.publishEvent(any(RequestEventDto.class)))
                .thenReturn(CompletableFuture.completedFuture(publishedEvent));

        var result = mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestEvent)))
                .andDo(print())
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorReason").value(errorReason))
                .andExpect(jsonPath("$.messageId").doesNotExist());
    }

    @Test
    void shouldCallPublisherWithCorrectEvent() throws Exception {

        String eventId = UUID.randomUUID().toString();
        String eventType = "test-event";
        String payload = "{\"key\":\"value\"}";
        
        RequestEventDto requestEvent = new RequestEventDto(eventId, eventType, payload);
        PublishedEventDto publishedEvent = PublishedEventDto.builder()
                .success(true)
                .messageId("message-id")
                .build();

        ArgumentCaptor<RequestEventDto> eventCaptor = forClass(RequestEventDto.class);
        when(publisher.publishEvent(any(RequestEventDto.class)))
                .thenReturn(CompletableFuture.completedFuture(publishedEvent));

        var result = mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestEvent)))
                .andDo(print())
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());


        verify(publisher).publishEvent(eventCaptor.capture());
        RequestEventDto capturedEvent = eventCaptor.getValue();
        assertEquals(eventId, capturedEvent.getId());
        assertEquals(eventType, capturedEvent.getType());
        assertEquals(payload, capturedEvent.getPayload());
    }
}

