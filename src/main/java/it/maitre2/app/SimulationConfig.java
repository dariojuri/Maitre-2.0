package it.maitre2.app;

import java.util.List;

public class SimulationConfig {

    //Tempo
    public final double durationMinutes;
    public final long seed;

    //Camerieri
    public final List<Double> waiterEfficiencies;

    //Sigmoide
    public final double minInterArrival;
    public final double maxInterArrival;
    public final double kSigmoid;
    public final int maxTables;

    //Cucina
    public final double kitchenBase;
    public final double kitchenPerPlate;
    public final double kitchenJitter;

    public SimulationConfig(double durationMinutes, long seed,
                            List<Double> waiterEfficiencies,
                            double minInterArrival, double maxInterArrival, double kSigmoid, int maxTables,
                            double kitchenBase, double kitchenPerPlate, double kitchenJitter)
    {
        this.durationMinutes = durationMinutes;
        this.seed = seed;

        this.waiterEfficiencies = waiterEfficiencies;

        this.minInterArrival = minInterArrival;
        this.maxInterArrival = maxInterArrival;
        this.kSigmoid = kSigmoid;
        this.maxTables = maxTables;

        this.kitchenBase = kitchenBase;
        this.kitchenPerPlate = kitchenPerPlate;
        this.kitchenJitter = kitchenJitter;

        validate();
    }


    private void validate(){
        if(durationMinutes <= 0) throw new IllegalArgumentException("durationMinutes must be > 0");
        if(minInterArrival <= 0) throw new IllegalArgumentException("minInterArrival must be > 0");
        if(maxInterArrival < minInterArrival) throw new IllegalArgumentException("maxInterArrival must be >= minInterArrival");
        if(kSigmoid <= 0) throw new IllegalArgumentException("kSigmoid must be > 0");
        if(maxTables <= 0) throw new IllegalArgumentException("maxTables must be > 0");
        if(kitchenBase < 0 || kitchenJitter < 0 || kitchenPerPlate < 0)
            throw new IllegalArgumentException("kitchen params must be > 0");
    }
}
