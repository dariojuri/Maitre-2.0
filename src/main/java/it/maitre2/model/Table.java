package it.maitre2.model;

public class Table {
    private int id;
    private int numClients;
    private TableState state;

    public Table(int id, int numClients, TableState initialState) {
        this.id = id;
        this.numClients = numClients;
        this.state = initialState;
    }

    public int getId() { return id; }

    public int getNumClients() { return numClients; }

    public TableState getState() { return state; }
    public void setState(TableState state, double now) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Table{id=" + id + ", numClients=" + numClients + ", state=" + state +"}";
    }
}
