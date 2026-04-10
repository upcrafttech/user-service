package com.upcraft.client;

import com.upcraft.dto.TaskDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Client for Task Service inter-service communication
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskServiceClient extends BaseServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.task.url:http://task-service:8084}")
    private String taskServiceUrl;

    public TaskDTO getTaskById(UUID taskId, String authToken) {
        log.info("Fetching task with ID: {}", taskId);
        String url = taskServiceUrl + "/api/tasks/" + taskId;
        return get("task-service", url, TaskDTO.class, authToken);
    }

    public TaskDTO createTask(TaskDTO taskDTO, String authToken) {
        log.info("Creating new task: {}", taskDTO.getTitle());
        String url = taskServiceUrl + "/api/tasks";
        return post("task-service", url, taskDTO, TaskDTO.class, authToken);
    }

    public TaskDTO updateTask(UUID taskId, TaskDTO taskDTO, String authToken) {
        log.info("Updating task: {}", taskId);
        String url = taskServiceUrl + "/api/tasks/" + taskId;
        return put("task-service", url, taskDTO, TaskDTO.class, authToken);
    }

    public boolean deleteTask(UUID taskId, String authToken) {
        log.info("Deleting task: {}", taskId);
        String url = taskServiceUrl + "/api/tasks/" + taskId;
        return delete("task-service", url, authToken);
    }

    public String approveTask(UUID taskId, String authToken) {
        log.info("Approving task: {}", taskId);
        String url = taskServiceUrl + "/api/tasks/" + taskId + "/approve";
        return post("task-service", url, null, String.class, authToken);
    }

    public String logTime(UUID taskId, double hours, String date, String authToken) {
        log.info("Logging time for task: {} - {} hours on {}", taskId, hours, date);
        String url = taskServiceUrl + "/api/tasks/" + taskId + "/timelogs?hours=" + hours + "&date=" + date;
        return post("task-service", url, null, String.class, authToken);
    }

    public TaskDTO getTasksByAssignee(UUID assigneeId, String authToken) {
        log.info("Fetching tasks for assignee: {}", assigneeId);
        String url = taskServiceUrl + "/api/tasks?assigneeId=" + assigneeId;
        return get("task-service", url, TaskDTO.class, authToken);
    }
}
