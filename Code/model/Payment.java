package model; 

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private Appointment appointment;
    private double amount;
    private String paymentMethod;
    private String staus; 
    private LocalDateTime paymentDateTime;

    public Payment(int paymentId, Appointment appointment, double amount, String paymentMethod) {
        this.paymentId = paymentId;
        this.appointment = appointment;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.staus = "Pending"; // Default status
        this.paymentDateTime = null; // Payment date will be set when the payment is processed
    }

    public int getPaymentId() {
        return paymentId;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return staus;
    }

    public LocalDateTime getPaymentDateTime() {
        return paymentDateTime;
    }

    //Updates the payment status.
    public void setStatus(String status) {
        this.staus = status;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    // Record the payment date and time when the payment is processed
    public void setPaymentDateTime(LocalDateTime paymentDateTime) {
        this.paymentDateTime = paymentDateTime;
    }
}