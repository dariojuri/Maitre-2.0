package it.maitre2;

import it.maitre2.agent.BestFirstStrategy;
import it.maitre2.agent.RoundRobinStrategy;
import it.maitre2.agent.Strategy;
import it.maitre2.kitchen.Kitchen;
import it.maitre2.metrics.MetricCollector;
import it.maitre2.model.*;
import it.maitre2.simulation.*;
import it.maitre2.util.RunLogger;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        long seed = 10L;

        //Crea camerieri
        List<Waiter> waiters = List.of(
                new Waiter(1, 1.0),
                new Waiter(2, 1.2),
                new Waiter(3, 1.2),
                new Waiter(4, 1.0),
                new Waiter(5, 0.8)
        );

        //Crea tavoli
        List<Table> tables = new ArrayList<>();



        DiningRoom room = new DiningRoom(waiters, tables);

        //Cucina
        Kitchen kitchen = new Kitchen(
                5.0,
                1.0,
                3.0,
                seed+1
        );


        //Scegli strategia -- ricorda di cambiare il nome del file del logger
        Strategy strategy = new BestFirstStrategy();
        //Strategy strategy = new RoundRobinStrategy();

        //Engine
        try(RunLogger logger = new RunLogger("run-bestfirst.log")) {
            MetricCollector metrics = new MetricCollector();

            double endTime = 300.0; //4 ore se sono "minuti"

            SimulationEngine engine = new SimulationEngine(room, kitchen, strategy, seed, logger, metrics, endTime);

            //Pianifica arrivi tavoli
            engine.schedule(new Event(0.0, EventType.ARRIVAL, -1));


            //Esegui simulazione
            logger.log("=== START SIMULATION (endTime=" + endTime + ") ===");

            //Loop manuale per stampare step-by-step
            while (engine.hasNextEvent() && engine.now() <= endTime) {
                engine.step();

                //snapshot dopo ogni step
                printSnapshot(engine, room);
            }

            logger.log("=== END SIMULATION at time=" + engine.now() + ") ===");
            printFinalWaiters(room, logger);
            printFinalTables(room, logger);

            logger.log(String.format("\n***METRICS | assignedTasks=" + metrics.assignedTasksCount()
                    + ", avgTaskWait=" + String.format("%.3f", metrics.avgTaskWait())
                    + ", workloadRange=" + String.format("%.3f", metrics.workloadRange(room.getWaiters()))
                    + ", utilizationCV=" + String.format("%.3f", metrics.utilizationCV(room.getWaiters(), endTime)))
            );
        }
    }

    private static void printSnapshot(SimulationEngine engine, DiningRoom room) {
        System.out.printf("time= %.2f | readyTask= %d | freeWaiters= %d%n\n",
                engine.now(),
                room.snapshotReadyTasks().size(),
                room.getFreeWaiter().size()
        );
    }

    private static void printFinalWaiters(DiningRoom room, RunLogger logger) {
        logger.log("\n--- WAITERS ---");
        for(Waiter waiter : room.getWaiters()) {
            logger.log(waiter.toString());
        }
    }

    private static void printFinalTables(DiningRoom room, RunLogger logger) {
        logger.log("\n--- TABLES ---");
        for(Table table : room.getTables()) {
            logger.log(table.toString());
        }
    }

}
