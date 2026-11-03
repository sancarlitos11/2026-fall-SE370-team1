package com.autodetailflow.dao;
import java.util.List;

import com.autodetailflow.model.Appointment;

public interface AppointmentDAOInterface {

    // Save new appointment / update existing. 
    void saveAppointment (Appointment appointment);

    // Retrive appointment by ID. 
    Appointment getAppointment(int appointmentId);

    // Retrive all appointments.
    List<Appointment> getAllAppointments();

    // Delete appointment by ID.
    void deleteAppointment(int appointmentId);
}