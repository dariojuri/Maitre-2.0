package it.maitre2.metrics;

import it.maitre2.model.Task;
import it.maitre2.model.Waiter;

import java.util.List;

public class MetricCollector {

    private double totalTaskWait = 0.0;
    private long assignedTasks = 0;

    //Conta il tempo di attesa totale per tutte le task e il numero totale delle task
    public void recordAssignment(Task task, double now){
        double wait = Math.max(0.0, now - task.getCreatedAt());
        totalTaskWait += wait;
        assignedTasks++;
    }

    //Calcola la metrica avgTaskWait ovvero l'attesa per task
    public double avgTaskWait(){
        return assignedTasks == 0 ? 0.0 : totalTaskWait / assignedTasks;
    }

    //Restituisce il numero di task contate
    public long assignedTasksCount(){
        return assignedTasks;
    }

    //Restituisce la metrica utilizationCV, ovvero il coefficiente di variazione dell'utilizzazione dei camerieri
    //Se utilizationCV = 0 -> carico perfettamente bilanciato
    public double utilizationCV(List<Waiter> waiters, double makespan){
        int n = waiters.size();
        if(n == 0) return 0.0;

        double[] doubles = new double[n];
        double sum = 0.0;

        //Calcoliamo la media delle utilizzazioni, quindi il livello medio di carico
        for(int i = 0; i < n; i++){
            Waiter waiter = waiters.get(i);
            double maxWork = makespan * waiter.getEfficiency();
            doubles[i] = maxWork == 0 ? 0 : waiter.getWorkloadTime() / maxWork;
            sum += doubles[i];
        }
        double mean = sum / n;
        if(mean == 0.0) return 0.0;

        //Calcoliamo la deviazione standard che misura quanto i camerieri sono diversi tra loro nel carico
        double var = 0.0;
        for(double x : doubles){
            double d = x- mean;
            var += d*d;
        }
        var/=n;
        double ds = Math.sqrt(var);

        //Restituiamo il coefficiente di variazione
        return ds / mean;
    }
}
