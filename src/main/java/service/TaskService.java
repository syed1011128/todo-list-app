package service;

import model.Task;
import java.util.ArrayList;
import java.util.List;

public class TaskService {
    private final ArrayList<Task> tasks = new ArrayList<>();

    public void addTask(String title) {
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Task title cannot be empty.");
        tasks.add(new Task(title.trim()));
    }

    public void deleteTask(int index) {
        tasks.remove(index);
    }

    public void markCompleted(int index) {
        tasks.get(index).setCompleted(true);
    }

    public List<Task> getAllTasks() {
        return tasks;
    }
}