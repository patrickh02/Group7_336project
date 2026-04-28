package com.flightreservation.model;

public class Airline {
    private String airlineId;
    private String name;

    public Airline() {}

    public Airline(String airlineId, String name) {
        this.airlineId = airlineId;
        this.name = name;
    }

    public String getAirlineId() {
        return airlineId;
    }

    public void setAirlineId(String airlineId) {
        this.airlineId = airlineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return airlineId + " - " + name;
    }
}
