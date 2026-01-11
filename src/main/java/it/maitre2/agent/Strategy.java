package it.maitre2.agent;

import it.maitre2.model.Task;
import it.maitre2.model.Waiter;
import java.util.List;

public interface Strategy {
    Assignment choose(List<Task> readyTasks, List<Waiter> freeWaiters, double now);
}
