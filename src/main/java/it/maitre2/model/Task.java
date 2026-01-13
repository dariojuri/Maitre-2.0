package it.maitre2.model;

import java.util.Objects;

public class Task {
    private final TaskType type;
    private final int tableId;
    private final double createdAt;
    private final int numPlates;

    public Task(TaskType type, int tableId, double createdAt, int numPlates) {
        this.type = Objects.requireNonNull(type);
        this.tableId = tableId;
        this.createdAt = createdAt;
        this.numPlates = numPlates;
    }

    public TaskType getType() { return type; }

    public int getTableId() { return tableId; }

    public double getCreatedAt() { return createdAt; }

    public int getNumPlates() { return numPlates; }

    @Override
    public String toString() {
        return "Task{type=" + type + ", tableId= " + tableId + ", createdAt=" + createdAt + ", numPlates=" + numPlates + "}";
    }
}
