package com.autodetailflow.model;

public class Vehicle { 
    private int vehicleId; 
    private User user;
    private String make;
    private String model; 
    private int year; 
    private String type; 

    public Vehicle(int vehicleId, User user, String make, String model, int year, String type) {
        this.vehicleId = vehicleId; 
        this.user = user; 
        this.make = make; 
        this.model = model; 
        this.year = year;
        this.type = type; 
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public User getUser() {
        return user;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model; 
    }
    
    public int getYear() {
        return year;
    }

    public String getType() {
        return type; 
    }
}
