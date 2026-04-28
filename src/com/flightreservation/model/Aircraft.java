package com.flightreservation.model;

public class Aircraft {
    private int aircraftId;
    private String model;
    private int capacity;
    private String airlineId;

    public Aircraft() {}

    public Aircraft(int aircraftId, String model, int capacity, String airlineId) {
        this.aircraftId = aircraftId;
        this.model = model;
        this.capacity = capacity;
        this.airlineId = airlineId;
    }

    public int getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(int aircraftId) {
        this.aircraftId = aircraftId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getAirlineId() {
        return airlineId;
    }

    public void setAirlineId(String airlineId) {
        this.airlineId = airlineId;
    }

    @Override
    public String toString() {
        return aircraftId + " - " + model + " (cap:" + capacity + ")";
    }
}
