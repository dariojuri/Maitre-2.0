package it.maitre2.model;

public class Table {
    private int id;
    private int numClients;
    private TableState state;
    private double waitingSince;  //Per la raccolta delle metriche

    public Table(int id, int numClients, TableState initialState, double now) {
        this.id = id;
        this.numClients = numClients;
        this.state = initialState;
        this.waitingSince = now;
    }

    public int getId() { return id; }
    public int getNumClients() { return numClients; }

    public TableState getState() { return state; }
    public void setState(TableState state, double now) {
        this.state = state;
        this.waitingSince = now; //quando entra in uno stato "di attesa", resettiamo il timestamp
    }

    public double getWaitingSince() { return waitingSince; }
    public void setWaitingSince(double t) { this.waitingSince = t; }

    @Override
    public String toString() {
        return "Table{id=" + id + ", numClients=" + numClients + ", state=" + state +"}";
    }
}
