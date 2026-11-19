
package com.autodetailflow.model;

public class DetailingPackage {

    private int packageId;
    private String name;
    private String description;
    private double price;
    private int duration; // in minutes

    public DetailingPackage(int packageId, String name,
            String description, double price, int duration) {

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

    // Calculate the price based on the vehicle category
    public double calculatePrice(String vehicleType) {

        if (vehicleType == null) {
            throw new IllegalArgumentException(
                "Please select a vehicle type"
            );
        }

        String type = vehicleType.trim().toLowerCase();

        int category;

        if (type.equals("coupe") || type.equals("sedan")) {
            category = 0;
        } else if (type.equals("small suv")) {
            category = 1;
        } else if (type.equals("large suv") || type.equals("truck")) {
            category = 2;
        } else if (type.equals("van")) {
            category = 3;
        } else {
            throw new IllegalArgumentException(
                "Invalid vehicle type"
            );
        }

        double[][] prices = {
            {199, 209, 229, -1},  // Ceramic Package
            {179, 189, 199, 229}, // Full Interior
            {129, 139, 149, 159}, // Full Exterior
            {279, 289, 319, 349}, // Elite Package
            {199, 209, 229, 249}, // Premium Package
            {169, 179, 189, 219}  // Deluxe Package
        };

        if (packageId < 1 || packageId > prices.length) {
            throw new IllegalArgumentException(
                "Invalid detailing package"
            );
        }

        double calculatedPrice = prices[packageId - 1][category];

        if (calculatedPrice == -1) {
            throw new IllegalArgumentException(
                "This package is not available for the selected vehicle type"
            );
        }

        return calculatedPrice;
    }
}
