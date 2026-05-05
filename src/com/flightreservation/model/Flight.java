package com.flightreservation.model;

import java.sql.Time;
import java.sql.Date;

public class Flight {
    private int flightId;
    private String flightNum;
    private String airlineId;
    private int aircraftId;
    private String depAirportId;
    private String arrAirportId;
    private Time depTime;
    private Time arrTime;
    private String type;
    private String daysOfWeek;
    private int    stops;
    private double economyPrice;
    private double businessPrice;
    private double firstPrice;

    private String airlineName;
    private String depCity;
    private String arrCity;
    private int capacity;
    private int bookedSeats;
    private Date searchDate;

    public Flight() {}

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public String getFlightNum() {
        return flightNum;
    }

    public void setFlightNum(String flightNum) {
        this.flightNum = flightNum;
    }

    public String getAirlineId() {
        return airlineId;
    }

    public void setAirlineId(String airlineId) {
        this.airlineId = airlineId;
    }

    public int getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(int aircraftId) {
        this.aircraftId = aircraftId;
    }

    public String getDepAirportId() {
        return depAirportId;
    }

    public void setDepAirportId(String depAirportId) {
        this.depAirportId = depAirportId;
    }

    public String getArrAirportId() {
        return arrAirportId;
    }

    public void setArrAirportId(String arrAirportId) {
        this.arrAirportId = arrAirportId;
    }

    public Time getDepTime() {
        return depTime;
    }

    public void setDepTime(Time depTime) {
        this.depTime = depTime;
    }

    public Time getArrTime() {
        return arrTime;
    }

    public void setArrTime(Time arrTime) {
        this.arrTime = arrTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public int getStops() { return stops; }
    public void setStops(int stops) { this.stops = stops; }

    public double getEconomyPrice() {
        return economyPrice;
    }

    public void setEconomyPrice(double economyPrice) {
        this.economyPrice = economyPrice;
    }

    public double getBusinessPrice() {
        return businessPrice;
    }

    public void setBusinessPrice(double businessPrice) {
        this.businessPrice = businessPrice;
    }

    public double getFirstPrice() {
        return firstPrice;
    }

    public void setFirstPrice(double firstPrice) {
        this.firstPrice = firstPrice;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public String getDepCity() {
        return depCity;
    }

    public void setDepCity(String depCity) {
        this.depCity = depCity;
    }

    public String getArrCity() {
        return arrCity;
    }

    public void setArrCity(String arrCity) {
        this.arrCity = arrCity;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public Date getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(Date searchDate) {
        this.searchDate = searchDate;
    }

    public int getAvailableSeats() {
        return capacity - bookedSeats;
    }

    public double getPriceForClass(String flightClass) {
        switch (flightClass.toLowerCase()) {
            case "business": return businessPrice;
            case "first":    return firstPrice;
            default:         return economyPrice;
        }
    }

    @Override
    public String toString() {
        return airlineId + flightNum + " " + depAirportId + "→" + arrAirportId;
    }
}
