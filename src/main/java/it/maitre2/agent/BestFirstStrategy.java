package it.maitre2.agent;

import it.maitre2.model.Task;
import it.maitre2.model.Waiter;

import java.util.List;

public class BestFirstStrategy implements Strategy {

    @Override
    public Assignment choose(List<Task> readyTasks, List<Waiter> freeWaiters, double now){

        Task bestTask = null;
        Waiter bestWaiter = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for(Task t : readyTasks){
            for(Waiter w : freeWaiters){
                double score = cost(t, w, now);
                if(score < bestScore){
                    bestScore = score;
                    bestTask = t;
                    bestWaiter = w;
                }
            }
        }
        return new Assignment(bestTask, bestWaiter);
    }


    private double cost(Task t, Waiter w, double now){
        //urgenza: più bassa = più urgente
        double urgency = switch(t.getType()){
            case SERVE_FOOD -> 0.0;
            case BILL -> 1.0;
            case TAKE_ORDER -> 2.0;
        };

        //attesa accumulata dal task, più aspetta = più urgente
        double waiting = now - t.getCreatedAt();

        //tempo stimato base per tipo / efficienza
        double base = switch(t.getType()){
            case TAKE_ORDER -> 2.0;
            case SERVE_FOOD -> 3.0 + 0.2 * t.getNumPlates();
            case BILL -> 1.5;
        };
        double est = base / w.getEfficiency();

        //penalizza camerieri già carichi con normalizzazione
        double load = w.getWorkloadTime() / w.getEfficiency();

        return (urgency*2.0) + (est*0.2) + (load*0.01) - (10.0*waiting);
    }

}
