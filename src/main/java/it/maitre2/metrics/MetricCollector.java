package it.maitre2.metrics;

import it.maitre2.model.Task;
import it.maitre2.model.Waiter;
import java.util.ArrayList;
import java.util.List;

public class MetricCollector {

    private double totalTaskWait = 0.0;
    private long assignedTasks = 0;

    //opzionale: per debug/analisi
    private final List<Double> taskWaitSamples = new ArrayList<>();

    public void recordAssignment(Task task, double now){
        double wait = Math.max(0.0, now - task.getCreatedAt());
        totalTaskWait += wait;
        assignedTasks++;
        taskWaitSamples.add(wait);
    }

    public double avgTaskWait(){
        return assignedTasks == 0 ? 0.0 : totalTaskWait / assignedTasks;
    }

    public long assignedTasksCount(){
        return assignedTasks;
    }

    public double workloadRange(List<Waiter> waiters){
        if(waiters.isEmpty()) return 0.0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for(Waiter waiter : waiters){
            double x = waiter.getWorkloadTime();
            min = Math.min(min, x);
            max = Math.max(max, x);
        }
        return max - min;
    }

    //Coefficiente di variazione (std/mean) 0 = perfettamente bilanciato
    public double utilizationCV(List<Waiter> waiters, double endTime){
        int n = waiters.size();
        if(n == 0) return 0.0;

        double[] doubles = new double[n];
        double sum = 0.0;

        for(int i = 0; i < n; i++){
            Waiter waiter = waiters.get(i);
            double maxWork = endTime * waiter.getEfficiency();
            doubles[i] = maxWork == 0 ? 0 : waiter.getWorkloadTime() / maxWork;
            sum += doubles[i];
        }

        double mean = sum / n;
        if(mean == 0.0) return 0.0;

        double var = 0.0;
        for(double x : doubles){
            double d = x- mean;
            var += d*d;
        }
        var/=n;
        return Math.sqrt(var) / mean;
    }
}
