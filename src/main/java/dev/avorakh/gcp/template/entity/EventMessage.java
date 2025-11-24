package dev.avorakh.gcp.template.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "event_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;

    @Column(name = "event_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> eventData;

    @Column(name = "message_id", length = 255)
    private String messageId;

    @Column(name = "created", length = 255)
    private String created;

    @Column(name = "modified", length = 255)
    private String modified;
}


