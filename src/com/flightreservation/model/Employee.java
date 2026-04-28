package com.flightreservation.model;

public class Employee {
    private int employeeId;
    private String name;
    private String email;
    private String type;
    private String password;

    public Employee() {}

    public Employee(String name, String email, String type, String password) {
        this.name = name;
        this.email = email;
        this.type = type;
        this.password = password;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return employeeId + " - " + name + " [" + type + "]";
    }
}
