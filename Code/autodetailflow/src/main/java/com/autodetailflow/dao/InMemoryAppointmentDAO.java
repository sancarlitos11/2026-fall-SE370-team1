package com.autodetailflow.dao;

import com.autodetailflow.model.Appointment;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAppointmentDAO implements AppointmentDAOInterface {

    // Store appiointment in memory.
    private final List<Appointment> appointments = new ArrayList<>();

    // Save new appointment / update existing.
    @Override
    public void saveAppointment(Appointment appointment) {
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getAppointmentId() == appointment.getAppointmentId()) {
                appointments.set(i, appointment);
                return;
            }
        }
        appointments.add(appointment);
    }

    // Retrieve appointment by ID.
    @Override
    public Appointment getAppointment(int appointmentId) {
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId() == appointmentId) {
                return appointment;
            }
        }
        return null;
    }

    // Retrieve all appointments.
    @Override
    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments);
    }

    // Delete appointment by ID.
    @Override
    public void deleteAppointment(int appointmentId) {
        appointments.removeIf(
            appointment -> appointment.getAppointmentId() == appointmentId
        );
    }
}