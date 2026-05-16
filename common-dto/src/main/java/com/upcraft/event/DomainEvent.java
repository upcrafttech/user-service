package com.upcraft.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent {
    private String eventId = UUID.randomUUID().toString();
    private LocalDateTime timestamp = LocalDateTime.now();
    private UUID tenantId;
    
    public DomainEvent(UUID tenantId) {
        this.tenantId = tenantId;
    }
    
    public abstract String getEventType();
}
