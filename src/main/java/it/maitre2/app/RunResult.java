package it.maitre2.app;

import it.maitre2.model.TableState;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RunResult {
    public final String strategyName;

    public final long assignedTasks;
    public final double avgTaskWait;

    public final double utilizationCV;

    public final Map<TableState, Integer> tablesByState;
    public final List<WaiterSnapshot> waiters;


    public final double makespanMinutes;

    public RunResult(String strategyName,
                     long assignedTasks, double avgTaskWait,
                     double utilizationCV,
                     Map<TableState, Integer> tablesByState, List<WaiterSnapshot> waiters, double makespanMinutes) {
        this.strategyName = strategyName;
        this.assignedTasks = assignedTasks;
        this.avgTaskWait = avgTaskWait;
        this.utilizationCV = utilizationCV;
        this.tablesByState = tablesByState;
        this.waiters = waiters;
        this.makespanMinutes = makespanMinutes;
    }

    public static Map<TableState, Integer> emptyStateMap(){
        EnumMap<TableState, Integer> map = new EnumMap<>(TableState.class);
        for(TableState state : TableState.values()){
            map.put(state, 0);
        }
        return map;
    }
}
