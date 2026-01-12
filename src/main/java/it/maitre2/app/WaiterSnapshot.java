package it.maitre2.app;

public class WaiterSnapshot {
    public final int id;
    public final double efficiency;
    public final double workloadMinutes;
    public final double utilization; //workload / (T*eff)

    public WaiterSnapshot(int id, double efficiency, double workloadMinutes, double utilization) {
        this.id = id;
        this.efficiency = efficiency;
        this.workloadMinutes = workloadMinutes;
        this.utilization = utilization;
    }
}
