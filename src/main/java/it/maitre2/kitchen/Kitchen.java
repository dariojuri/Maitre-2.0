package it.maitre2.kitchen;

import it.maitre2.simulation.Event;
import it.maitre2.simulation.EventType;
import it.maitre2.simulation.SimulationEngine;

import java.util.Random;

public class Kitchen {

    private final double baseMinutes;
    private final double perPlateMinutes;
    private final double jitterMaxMinutes;
    private final Random rng;

    public Kitchen(double baseMinutes, double perPlateMinutes, double jitterMaxMinutes, long seed) {
        this.baseMinutes = baseMinutes;
        this.perPlateMinutes = perPlateMinutes;
        this.jitterMaxMinutes = jitterMaxMinutes;
        this.rng = new Random(seed);
    }

    //Pianifica l'evento FOOD_READY a partire da (now) e dal numero di piatti
    public void scheduleFoodReady(SimulationEngine engine, double now, int tableId, int plates) {
        double prep = baseMinutes + perPlateMinutes*plates + jitter();
        engine.schedule(new Event(now + prep, EventType.FOOD_READY, tableId));
    }

    private double jitter() {
        return rng.nextDouble() * jitterMaxMinutes;
    }
}
