package com.example.hello.repo;

import com.example.hello.model.Task;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryStore {
    private AtomicInteger taskId = new AtomicInteger(1000);
    private Map<Integer, Task> tasks = new ConcurrentHashMap<>();

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Task save(Task task) {
        Task newTask = task;
        if (task.id() == 0) {     // new one
            newTask = new Task(taskId.incrementAndGet(), task.name(), task.description());
        }
        tasks.put(newTask.id(), newTask);
        return newTask;
    }

    public Task delete(int id) {
        return tasks.remove(id);
    }

    public List<Task> getTasks() {
        return tasks.values().stream()
                .sorted((t1, t2) -> t1.id() - t2.id())
                .toList();
    }
}
