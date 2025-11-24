package dev.avorakh.gcp.template.repository;

import dev.avorakh.gcp.template.entity.EventMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventMessageRepository extends JpaRepository<EventMessage, UUID> {
}


