package com.example.hello.service;

import com.example.hello.model.Task;
import com.example.hello.repo.InMemoryStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final InMemoryStore inMemoryStore;

    public TaskService(InMemoryStore inMemoryStore) {
        this.inMemoryStore = inMemoryStore;
    }

    public List<Task> getTasks() {
        return inMemoryStore.getTasks();
    }

    public Task getTask(int id) {
        return inMemoryStore.getTask(id);
    }

    public Task newTask(Task task) {
        return inMemoryStore.save(task);
    }

    public Task updateTask(Task task) {
        return inMemoryStore.save(task);
    }

    public Task deleteTask(int id) {
        return inMemoryStore.delete(id);
    }
}
