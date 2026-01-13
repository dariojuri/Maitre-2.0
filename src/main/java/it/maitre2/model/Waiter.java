package it.maitre2.model;

public class Waiter {

    private final int id;
    private final double efficiency; // >0, 0.8 lento, 1.0 medio, 1.2 veloce
    private boolean busy;
    private double workloadTime; //tempo di lavoro

    public Waiter(int id, double efficiency) {
        if (efficiency<=0) throw new IllegalArgumentException("efficiency must be greater than zero");
        this.id = id;
        this.efficiency = efficiency;
        this.busy = false;
        this.workloadTime = 0.0;
    }

    public int getId() { return id; }

    public double getEfficiency() { return efficiency; }

    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) { this.busy = busy; }

    public double getWorkloadTime() { return workloadTime; }
    public void addWorkloadTime(double deltaWorkloadTime) {
        if (deltaWorkloadTime <= 0) throw new IllegalArgumentException("deltaWorkloadTime must be greater than zero");
        this.workloadTime += deltaWorkloadTime;
    }

    @Override
    public String toString() {
        return "Waiter{id= " + id +", eff= " + efficiency + ", busy= " + busy + ", workloadTime= " + workloadTime + "}";
    }
}
