package com.upcraft.task.event;

import com.upcraft.dto.TaskDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TaskEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${task.events.exchange:task-events-exchange}")
    private String exchange;

    public void publishTaskCreated(TaskDTO task) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "TaskCreated");
        payload.put("taskId", task.getId());
        payload.put("tenantId", task.getTenantId());
        payload.put("assigneeId", task.getAssigneeId());
        payload.put("title", task.getTitle());
        rabbitTemplate.convertAndSend(exchange, "task.created", payload);
    }

    public void publishTaskApproved(TaskDTO task) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "TaskApproved");
        payload.put("taskId", task.getId());
        payload.put("tenantId", task.getTenantId());
        payload.put("assigneeId", task.getAssigneeId());
        payload.put("approvedBy", task.getApprovedBy());
        payload.put("bonusAmount", task.getBonusAmount());
        rabbitTemplate.convertAndSend(exchange, "task.approved", payload);
    }
}

