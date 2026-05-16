package com.upcraft.task.config;

import com.upcraft.dto.TaskDTO;
import com.upcraft.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskIntegrationConsumers {

    private final TaskService taskService;

    @RabbitListener(queues = "${task.events.queue:task.events.queue}")
    public void onTaskEvents(String rawEvent) {
        log.info("Task integration consumer received event payload: {}", rawEvent);
    }

    @RabbitListener(queues = "${task.integration.template.queue:task.integration.template.queue}")
    public void onTemplateCreate(TaskDTO templateSeed) {
        try {
            if (templateSeed == null) {
                return;
            }
            taskService.createTask(templateSeed);
            log.info("Integration consumer created task from template seed for tenant {}", templateSeed.getTenantId());
        } catch (Exception ex) {
            log.error("Failed to process integration template message", ex);
        }
    }
}
