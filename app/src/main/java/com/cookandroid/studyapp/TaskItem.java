package com.cookandroid.studyapp;

import java.util.HashMap;
import java.util.Map;

// TaskItem.java
public class TaskItem {
    private String taskId;
    private String taskName;
    private String duration = "0h 0m 0s";
    private boolean completed = false;

    public TaskItem(String taskId, String taskName, boolean completed) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.completed = completed;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", taskId);
        map.put("taskName", taskName);
        map.put("duration", duration);
        map.put("isCompleted", completed);
        return map;
    }
}
