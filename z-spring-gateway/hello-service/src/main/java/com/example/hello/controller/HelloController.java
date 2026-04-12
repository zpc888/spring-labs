package com.example.hello.controller;

import com.example.hello.model.Task;
import com.example.hello.service.TaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class HelloController {
    private final TaskService taskService;

    public HelloController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Value("${server.port}")
    private int port;

    @GetMapping("/hello")
    public String hello(@RequestParam(required = false) Map<String, String> params) {
        String baseMessage = "hello world from port: " + port;
        if (params != null && !params.isEmpty()) {
            return baseMessage + " | params: " + params;
        }
        return baseMessage;
    }

    @PostMapping("/tasks")
    public Task createTask(@RequestBody Task task) {
        return taskService.newTask(task);
    }

    @GetMapping("/tasks")
    public List<Task> getTasks() {
        return taskService.getTasks();
    }

    @PutMapping("/tasks/{taskId}")
    public Task updateTask(@PathVariable Integer taskId, @RequestBody Task task) {
        Task target = new Task(taskId, task.name(), task.description());
        return taskService.updateTask(target);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<Task> getTask(@PathVariable Integer taskId) {
        Task t = taskService.getTask(taskId);
        if (t == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(t);
        }
    }


    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Task> deleteTask(@PathVariable Integer taskId) {
        Task t = taskService.deleteTask(taskId);
        if (t == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.ok(t);
        }
    }
}

