package com.flightreservation.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Waitlist {
    private int customerId;
    private int flightId;
    private int position;
    private Timestamp requestTime;
    private Date depDate;
    private String flightClass;

    private String customerName;
    private String customerEmail;
    private String flightNum;
    private String airlineId;
    private String depAirportId;
    private String arrAirportId;

    public Waitlist() {}

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }

    public Date getDepDate() {
        return depDate;
    }

    public void setDepDate(Date depDate) {
        this.depDate = depDate;
    }

    public String getFlightClass() {
        return flightClass;
    }

    public void setFlightClass(String flightClass) {
        this.flightClass = flightClass;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
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
}
