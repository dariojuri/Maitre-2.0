package it.maitre2.simulation;

public class EngineConfig {
    public final double durationMinutes;

    public final double minInterArrival;
    public final double maxInterArrival;
    public final double kSigmoid;
    public final int maxTables;

    public EngineConfig(double durationMinutes, double minInterArrival, double maxInterArrival, double kSigmoid, int maxTables) {
        this.durationMinutes = durationMinutes;
        this.minInterArrival = minInterArrival;
        this.maxInterArrival = maxInterArrival;
        this.kSigmoid = kSigmoid;
        this.maxTables = maxTables;

        validate();
    }

    private void validate() {
        if(durationMinutes <= 0) throw new IllegalArgumentException("durationMinutes must be greater than zero");
        if(minInterArrival <= 0) throw new IllegalArgumentException("minInterArrival must be greater than zero");
        if(maxInterArrival < minInterArrival) throw new IllegalArgumentException("maxInterArrival must be greater than minInterArrival");
        if(kSigmoid <= 0) throw new IllegalArgumentException("kSigmoid must be greater than zero");
        if(maxTables <= 0) throw new IllegalArgumentException("maxTables must be greater than zero");
    }
}
