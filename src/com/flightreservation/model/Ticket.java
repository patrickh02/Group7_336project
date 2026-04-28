package com.flightreservation.model;

import java.sql.Timestamp;

public class Ticket {
    private int ticketNum;
    private int customerId;
    private double totalFare;
    private Timestamp purchaseDatetime;
    private double bookingFee;
    private String type;
    private boolean flexible;
    private String status;

    private String customerName;

    public Ticket() {}

    public int getTicketNum() {
        return ticketNum;
    }

    public void setTicketNum(int ticketNum) {
        this.ticketNum = ticketNum;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }

    public Timestamp getPurchaseDatetime() {
        return purchaseDatetime;
    }

    public void setPurchaseDatetime(Timestamp purchaseDatetime) {
        this.purchaseDatetime = purchaseDatetime;
    }

    public double getBookingFee() {
        return bookingFee;
    }

    public void setBookingFee(double bookingFee) {
        this.bookingFee = bookingFee;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isFlexible() {
        return flexible;
    }

    public void setFlexible(boolean flexible) {
        this.flexible = flexible;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
