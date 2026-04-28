package com.flightreservation.model;

import java.sql.Date;
import java.sql.Time;

public class TicketFlight {
    private int ticketNum;
    private int flightId;
    private int sequenceOrder;
    private Date depDate;
    private String seatNum;
    private String mealPref;
    private String flightClass;

    private String flightNum;
    private String airlineId;
    private String airlineName;
    private String depAirportId;
    private String arrAirportId;
    private String depCity;
    private String arrCity;
    private Time depTime;
    private Time arrTime;

    public TicketFlight() {}

    public int getTicketNum() {
        return ticketNum;
    }

    public void setTicketNum(int ticketNum) {
        this.ticketNum = ticketNum;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public int getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(int sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public Date getDepDate() {
        return depDate;
    }

    public void setDepDate(Date depDate) {
        this.depDate = depDate;
    }

    public String getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(String seatNum) {
        this.seatNum = seatNum;
    }

    public String getMealPref() {
        return mealPref;
    }

    public void setMealPref(String mealPref) {
        this.mealPref = mealPref;
    }

    public String getFlightClass() {
        return flightClass;
    }

    public void setFlightClass(String flightClass) {
        this.flightClass = flightClass;
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

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
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
}
