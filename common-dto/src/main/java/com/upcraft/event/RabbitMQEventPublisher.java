package com.upcraft.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(String exchange, String routingKey, DomainEvent event) {
        log.debug("Publishing event: {} to exchange: {} with routing key: {}", event.getEventType(), exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
