package com.upcraft.event;

public interface EventPublisher {
    void publish(String exchange, String routingKey, DomainEvent event);
}
