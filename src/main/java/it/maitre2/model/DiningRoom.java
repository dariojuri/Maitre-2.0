package it.maitre2.model;

import java.util.*;

public class DiningRoom {
    private final Map<Integer, Table> tablesById;
    private final List<Waiter> waiters;
    private final Deque<Task> readyTasks;

    public DiningRoom(List<Waiter> waiters, List<Table> tables) {
        if(waiters==null || waiters.isEmpty())
            throw new IllegalArgumentException("waiters is null or empty");
        if(tables==null || tables.isEmpty())
            throw new IllegalArgumentException("tables is null or empty");

        this.waiters = new ArrayList<>(waiters);
        this.tablesById = new HashMap<>();
        for(Table t : tables) {
            if(tablesById.containsKey(t.getId()))
                throw new IllegalArgumentException("table with id " + t.getId() + " already exists");
            tablesById.put(t.getId(), t);
        }
        this.readyTasks = new ArrayDeque<>();

    }

    public void addTable(Table table) {
        Objects.requireNonNull(table);
        if(tablesById.containsKey(table.getId())) {
            throw new IllegalArgumentException("Table with id " + table.getId() + " already exists");
        }
        tablesById.put(table.getId(), table);
    }

    public Table getTable(int tableId) {
        Table t = tablesById.get(tableId);
        if(t == null) { throw new IllegalArgumentException("Table with id " + tableId + " does not exist"); }
        return t;
    }

    public Collection<Table> getTables() {
        return Collections.unmodifiableCollection(tablesById.values());
    }

    public void addWaiter(Waiter waiter) {
        Objects.requireNonNull(waiter);
        waiters.add(waiter);
    }

    public List<Waiter> getWaiters() {
        return Collections.unmodifiableList(waiters);
    }

    public List<Waiter> getFreeWaiter() {
        List<Waiter> freeWaiters = new ArrayList<>();
        for(Waiter w : waiters) {
            if (!w.isBusy()) freeWaiters.add(w);
        }
        return freeWaiters;
    }

    public void pushReadyTask(Task task) {
        Objects.requireNonNull(task);
        readyTasks.add(task);
    }

    public boolean hasReadyTasks() {
        return !readyTasks.isEmpty();
    }

    public List<Task> snapshotReadyTasks() {
        return List.copyOf(readyTasks);
    }

    public Task popReadyTask() {
        return readyTasks.pollFirst();
    }

    public void removeReadyTask(Task task) {
        readyTasks.remove(task);
    }
}
