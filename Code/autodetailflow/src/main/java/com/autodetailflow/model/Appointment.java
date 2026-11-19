
package com.autodetailflow.model;

import java.time.LocalDateTime;

public class Appointment {

    private int appointmentId;
    private User user;
    private Vehicle vehicle;
    private DetailingPackage detailingPackage;
    private LocalDateTime appointmentDateTime;

    // Address where the mobile detailing service will happen
    private String serviceAddress;

    // Pending, Confirmed, Declined, Scheduled, Completed, Cancelled
    private String status;

    // Original constructor
    // Kept so existing code and tests continue to work
    public Appointment(
            int appointmentId,
            User user,
            Vehicle vehicle,
            DetailingPackage detailingPackage,
            LocalDateTime appointmentDateTime) {

        this.appointmentId = appointmentId;
        this.user = user;
        this.vehicle = vehicle;
        this.detailingPackage = detailingPackage;
        this.appointmentDateTime = appointmentDateTime;
        this.serviceAddress = "";
        this.status = "Pending";
    }

    // New constructor that includes the service address
    public Appointment(
            int appointmentId,
            User user,
            Vehicle vehicle,
            DetailingPackage detailingPackage,
            LocalDateTime appointmentDateTime,
            String serviceAddress) {

        this(
                appointmentId,
                user,
                vehicle,
                detailingPackage,
                appointmentDateTime
        );

        this.serviceAddress = serviceAddress;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public User getUser() {
        return user;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public DetailingPackage getDetailingPackage() {
        return detailingPackage;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    // Returns the address for the detailing appointment
    public String getServiceAddress() {
        return serviceAddress;
    }

    // Updates the service address
    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getStatus() {
        return status;
    }

    // Updates the appointment status
    public void setStatus(String status) {
        this.status = status;
    }
}
