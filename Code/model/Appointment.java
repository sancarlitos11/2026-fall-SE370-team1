package model; 

import java.time.LocalDateTime;

public class Appointment {
    private int appointmentId;
    private User user;
    private Vehicle vehicle;
    private DetailingPackage detailingPackage;
    private LocalDateTime appointmentDateTime;
    private String status; // "Pending", "Scheduled", "Completed", "Cancelled", "Declined"

    public Appointment(int appointmentId, User user, Vehicle vehicle, DetailingPackage detailingPackage, LocalDateTime appointmentDateTime) {
        this.appointmentId = appointmentId;
        this.user = user;
        this.vehicle = vehicle;
        this.detailingPackage = detailingPackage;
        this.appointmentDateTime = appointmentDateTime;
        this.status = "Pending"; // Default status
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

    public String getStatus() {
        return status;
    }

    //Updates the appointment status. 
    public void setStatus(String status) {
        this.status = status;
    }
}
