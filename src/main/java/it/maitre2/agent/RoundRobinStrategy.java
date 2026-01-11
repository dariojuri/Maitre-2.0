package it.maitre2.agent;

import it.maitre2.model.Task;
import it.maitre2.model.Waiter;

import java.util.List;

public class RoundRobinStrategy implements Strategy {
    private int nextIndex = 0;

    @Override
    public Assignment choose(List<Task> readyTasks, List<Waiter> freeWaiters, double now){
        if(readyTasks.isEmpty() || freeWaiters.isEmpty()){ return null; }

        Task task = readyTasks.get(0); //FIFO
        Waiter w = freeWaiters.get(nextIndex % readyTasks.size());
        nextIndex++;
        return new Assignment(task, w);
    }
}
