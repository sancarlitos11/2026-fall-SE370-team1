package com.autodetailflow.model;

public class DetailingPackage {
    private int packageId;
    private String name;
    private String description;
    private double price;
    private int duration; // in minutes 

    public DetailingPackage(int packageId, String name, String description, double price, int duration) {
        this.packageId = packageId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.duration = duration;
    }

    public int getPackageId() {
        return packageId;
    }

    public String getPackageName() {
        return name;
    }

    public String getPackageDescription() {
        return description;
    }

    public double getPackagePrice() {
        return price;
    }

    public int getPackageDuration() {
        return duration;
    }
}
