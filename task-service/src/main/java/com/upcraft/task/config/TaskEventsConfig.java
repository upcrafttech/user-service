package com.upcraft.task.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskEventsConfig {

    @Bean
    public TopicExchange taskEventsExchange() {
        return new TopicExchange("task-events-exchange", true, false);
    }

    @Bean
    public Queue taskEventsQueue() {
        return new Queue("task-events", true);
    }

    @Bean
    public Binding taskCreatedBinding(Queue taskEventsQueue, TopicExchange taskEventsExchange) {
        return BindingBuilder.bind(taskEventsQueue).to(taskEventsExchange).with("task.created");
    }

    @Bean
    public Binding taskApprovedBinding(Queue taskEventsQueue, TopicExchange taskEventsExchange) {
        return BindingBuilder.bind(taskEventsQueue).to(taskEventsExchange).with("task.approved");
    }
}

