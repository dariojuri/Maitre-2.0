package it.maitre2.app;

import it.maitre2.agent.Strategy;
import it.maitre2.kitchen.Kitchen;
import it.maitre2.metrics.MetricCollector;
import it.maitre2.model.DiningRoom;
import it.maitre2.model.Table;
import it.maitre2.model.TableState;
import it.maitre2.model.Waiter;
import it.maitre2.simulation.EngineConfig;
import it.maitre2.simulation.Event;
import it.maitre2.simulation.EventType;
import it.maitre2.simulation.SimulationEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimulationRunner {

    public static RunResult run(SimulationConfig cfg, Strategy strategy, boolean saveCsv) {

        //Genera camerieri
        List<Waiter> waiters = generateWaiters(cfg);

        //Sala inizialmente senza tavoli, arrivano con ARRIVAL
        List<Table> tables = List.of();
        DiningRoom room = new DiningRoom(waiters, tables);

        //Cucina
        Kitchen kitchen = new Kitchen(cfg.kitchenBase, cfg.kitchenPerPlate, cfg.kitchenJitter, cfg.seed);

        //Metriche
        MetricCollector metrics = new MetricCollector();

        //Engine
        EngineConfig engineCfg = new EngineConfig(cfg.durationMinutes, cfg.minInterArrival,
                cfg.maxInterArrival, cfg.kSigmoid, cfg.maxTables);

        //SimulationEngine
        SimulationEngine engine = new SimulationEngine(room, kitchen, strategy,
                cfg.seed, metrics, engineCfg);

        //Kickstart arrivi
        engine.schedule(new Event(0.0, EventType.ARRIVAL,-1 ));

        //Run
        engine.runUntil(cfg.durationMinutes);

        //Prepara risultato
        Map<TableState, Integer> counts = RunResult.emptyStateMap();
        for(Table table: room.getTables()){
            counts.put(table.getState(), counts.get(table.getState()) + 1);
        }

        List<WaiterSnapshot> ws = new ArrayList<>();
        for(Waiter waiter : room.getWaiters()){
            double denom = cfg.durationMinutes * waiter.getEfficiency();
            double util = denom == 0.0 ? 0.0 : waiter.getWorkloadTime() / denom;
            ws.add(new WaiterSnapshot(waiter.getId(), waiter.getEfficiency(), waiter.getWorkloadTime(), util));
        }


        /*
        Non utilizziamo cfg.durationMinutes, perchè porta ad errori di calcolo nel caso
        in cui la simulazione finisce prima della fine impostata.
        */
        double makespan = engine.now();
        double utilCV = metrics.utilizationCV(room.getWaiters(), makespan);

        RunResult r = new RunResult(strategy.getClass().getSimpleName(),
                metrics.assignedTasksCount(),
                metrics.avgTaskWait(),
                utilCV, counts, ws, makespan);

        //Esporta CSV
        if(saveCsv){
            ResultExporter.appendRow("result.csv", r, cfg.seed, cfg.durationMinutes, makespan);
        }

        return r;
    }

    private static List<Waiter> generateWaiters(SimulationConfig cfg){
        List<Waiter> waiters = new ArrayList<>();
        int id=1;

        for(double eff : cfg.waiterEfficiencies){
            waiters.add(new Waiter(id++, eff));
        }
        return waiters;

    }
}
