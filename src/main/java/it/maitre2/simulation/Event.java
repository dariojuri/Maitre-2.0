package it.maitre2.simulation;

import it.maitre2.model.Task;
import it.maitre2.model.TaskType;

public class Event implements Comparable<Event> {
    private final double time;
    private final EventType type;
    private final int tableId;

    private final int waiterId;
    private final TaskType taskType;

    public Event(double time, EventType type, int tableId, int waiterId, TaskType taskType) {
        this.time = time;
        this.type = type;
        this.tableId = tableId;
        this.waiterId = waiterId;
        this.taskType = taskType;
    }

    //evento "normale"
    public Event(double time, EventType type, int tableId) {
        this(time, type, tableId, -1, null);
    }

    //eventi di completamento task
    public Event(double time, int tableId, int waiterId, TaskType taskType) {
        this(time, EventType.TASK_DONE, tableId, waiterId, taskType);
    }

    public double getTime() { return time; }
    public EventType getType() { return type; }
    public int getTableId() { return tableId; }
    public int getWaiterId() { return waiterId; }
    public TaskType getTaskType() { return taskType; }

    @Override
    public int compareTo(Event other){
        return Double.compare(this.time, other.time);
    }

    @Override
    public String toString() {
        return "Event[" + type +", time= " + time + ", table= " + tableId + (type == EventType.TASK_DONE ? (", waiter=" + waiterId + ", task= " + taskType): "") + "]";
    }
}
