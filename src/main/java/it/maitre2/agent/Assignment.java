package it.maitre2.agent;

import it.maitre2.model.Task;
import it.maitre2.model.Waiter;

public record Assignment(Task task, Waiter waiter) {}
