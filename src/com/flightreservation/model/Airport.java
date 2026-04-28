package com.flightreservation.model;

public class Airport {
    private String airportId;
    private String name;
    private String city;
    private String country;

    public Airport() {}

    public Airport(String airportId, String name, String city, String country) {
        this.airportId = airportId;
        this.name = name;
        this.city = city;
        this.country = country;
    }

    public String getAirportId() {
        return airportId;
    }

    public void setAirportId(String airportId) {
        this.airportId = airportId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return airportId + " - " + city;
    }
}
