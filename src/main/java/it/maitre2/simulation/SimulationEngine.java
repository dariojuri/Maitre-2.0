package it.maitre2.simulation;

import it.maitre2.util.RunLogger;
import it.maitre2.agent.Assignment;
import it.maitre2.agent.Strategy;
import it.maitre2.kitchen.Kitchen;
import it.maitre2.model.*;

import java.util.Random;
import java.util.PriorityQueue;

public class SimulationEngine {

    private final RunLogger logger;

    private final DiningRoom room;
    private final Kitchen kitchen;
    private final Strategy strategy;
    private final PriorityQueue<Event> eventQueue = new PriorityQueue<>();
    private final Random rng;
    private double now = 0.0;

    public SimulationEngine(DiningRoom room, Kitchen kitchen, Strategy strategy, long seed, RunLogger logger) {
        this.room = room;
        this.kitchen = kitchen;
        this.strategy = strategy;
        rng = new Random(seed);
        this.logger = logger;
    }

    public double now() { return now; }

    public void schedule(Event e) {
        eventQueue.add(e);
    }

    public boolean hasNextEvent () {
        return !eventQueue.isEmpty();
    }

    public void runUntil(double endTime){
        while(hasNextEvent() && eventQueue.peek().getTime() <= endTime){
            step();
        }
    }

    public void step() {
        Event e = eventQueue.poll();

        logger.log("\n---EVENT-> " + e);

        if(e == null) return;

        now = e.getTime();
        handleEvent(e);
        dispatchAssignment();
    }

    private void handleEvent(Event e) {
        int tableId = e.getTableId();
        Table table = room.getTable(tableId);

        switch(e.getType()){
            case NEW_TABLE -> {
                //il tavolo entra nello stato "ready to order" dopo un piccolo delay
                table.setState(TableState.NEW, now);
                schedule(new Event(now + delay(0.5, 1.5),EventType.READY_TO_ORDER, tableId));
            }

            case READY_TO_ORDER -> {
                table.setState(TableState.WAITING_ORDER, now);
                room.pushReadyTask(new Task(TaskType.TAKE_ORDER, tableId, now, 0));

                //dopo che ordina, parte la cucina e poi arriva FOOD_READY
                int plates = samplePlates(table.getNumClients());
                kitchen.scheduleFoodReady(this, now,tableId, plates);

            }

            case FOOD_READY ->{
                table.setState(TableState.WAITING_FOOD, now);
                int plates = table.getNumClients();
                room.pushReadyTask(new Task(TaskType.SERVE_FOOD, tableId, now, plates));

                //dopo servizio + "consumo pasto" + il tavolo chiederà il conto
                schedule(new Event(now + delay(10, 25), EventType.BILL_REQUEST, tableId));
            }

            case BILL_REQUEST ->{
                table.setState(TableState.WAITING_BILL, now);
                room.pushReadyTask(new Task(TaskType.BILL, tableId , now , 0 ));
            }

            case TASK_DONE -> {
                int waiterId = e.getWaiterId();
                TaskType doneType = e.getTaskType();

                //libera cameriere
                Waiter doneWaiter = room.getWaiterById(waiterId);
                doneWaiter.setBusy(false);

                logger.log("---DONE -> waiter=" + waiterId + "does " + doneType + "for table=" + tableId + ")");

                //aggiornamento di stato tavolo
                Table t = room.getTable(tableId);
                if(doneType == TaskType.TAKE_ORDER){
                    t.setState(TableState.WAITING_FOOD, now);
                } else if(doneType == TaskType.SERVE_FOOD){

                } else if(doneType == TaskType.BILL){
                    t.setState(TableState.DONE, now);
                }
            }
        }
        }

        private void dispatchAssignment() {
            while(room.hasReadyTasks() && !room.getFreeWaiter().isEmpty()){
                var tasks = room.snapshotReadyTasks();
                var free = room.getFreeWaiter();

                Assignment a = strategy.choose(tasks, free, now);
                if(a == null) return;

                Task task = a.task();
                Waiter waiter = a.waiter();

                //rimuovi task dalla coda
                room.removeReadyTask(task);

                //marca cameriere come occupato
                waiter.setBusy(true);

                //durata stimata
                double duration = estimateTaskDurationMinutes(task, waiter);

                logger.log("---ASSIGN -> waiter " + waiter.getId() + " does " + task + "(duration=" + duration + ")");

                //aggiorna carico
                waiter.addWorkloadTime(duration);

                //schedula fine task
                schedule(new Event(now + duration, task.getTableId(), waiter.getId(), task.getType()));
            }
        }

        private double estimateTaskDurationMinutes(Task t, Waiter w) {
            double base = switch(t.getType()){
                case TAKE_ORDER -> 2.0;
                case SERVE_FOOD -> 3.0 + 0.2*t.getNumPlates();
                case BILL -> 1.5;
            };
            return base / w.getEfficiency();
        }

        private double delay(double min, double max) {
        return min + rng.nextDouble() * (max - min);
        }

        private int samplePlates(int numClients) {
        //minimal: un piatto a persona
            return Math.max(1, numClients / 2);
        }

}
