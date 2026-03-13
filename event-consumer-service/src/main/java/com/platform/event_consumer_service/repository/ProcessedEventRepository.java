package com.platform.event_consumer_service.repository;

import com.platform.event_consumer_service.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent,Long> {

    // Check if event already processed (avoid duplicates)
    boolean existsByEventId(String eventId);
}
