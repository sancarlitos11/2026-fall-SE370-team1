
package com.autodetailflow.service;

import com.autodetailflow.model.Appointment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

    private final List<Appointment> appointments =
            new ArrayList<>();

    private final AvailabilityService availabilityService;

    public AppointmentService(
            AvailabilityService availabilityService) {

        this.availabilityService = availabilityService;
    }

    // Returns all appointments
    public synchronized List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments);
    }

    // Adds a new appointment if the time is available
    public synchronized void addAppointment(
            Appointment appointment) {

        if (appointment == null ||
                appointment.getAppointmentDateTime() == null ||
                appointment.getDetailingPackage() == null) {

            throw new IllegalArgumentException(
                    "Appointment information is incomplete"
            );
        }

        LocalDateTime requestedStart =
                appointment.getAppointmentDateTime();

        int requestedDuration =
                appointment.getDetailingPackage()
                        .getPackageDuration();

        if (requestedDuration <= 0) {
            throw new IllegalArgumentException(
                    "Appointment duration must be positive"
            );
        }

        // Only active appointments reserve time
        if (availabilityService.blocksAvailability(
                appointment.getStatus())) {

            // Reject times blocked by an employee
            if (availabilityService.isBlocked(
                    requestedStart,
                    requestedDuration)) {

                throw new IllegalArgumentException(
                        "This appointment time is blocked by an employee"
                );
            }

            // Reject overlapping appointments
            for (Appointment existing : appointments) {

                if (availabilityService.blocksAvailability(
                        existing.getStatus())) {

                    LocalDateTime existingStart =
                            existing.getAppointmentDateTime();

                    int existingDuration =
                            existing.getDetailingPackage()
                                    .getPackageDuration();

                    boolean available =
                            availabilityService.isTimeAvailable(
                                    requestedStart,
                                    requestedDuration,
                                    existingStart,
                                    existingDuration
                            );

                    if (!available) {
                        throw new IllegalArgumentException(
                                "This appointment time is already booked"
                        );
                    }
                }
            }
        }

        appointments.add(appointment);
    }

    // Finds an appointment using its ID
    public synchronized Appointment getAppointmentById(
            int appointmentId) {

        for (Appointment appointment : appointments) {

            if (appointment.getAppointmentId() == appointmentId) {
                return appointment;
            }
        }

        throw new IllegalArgumentException(
                "Appointment not found"
        );
    }

    // Confirms a pending appointment request
    public synchronized Appointment confirmAppointment(
            int appointmentId) {

        Appointment appointment =
                getAppointmentById(appointmentId);

        // Only pending appointments can be confirmed
        if (!appointment.getStatus().equalsIgnoreCase("Pending")) {

            throw new IllegalArgumentException(
                    "Only pending appointments can be confirmed"
            );
        }

        appointment.setStatus("Confirmed");

        return appointment;
    }

    // Declines a pending appointment request
    public synchronized Appointment declineAppointment(
            int appointmentId) {

        Appointment appointment =
                getAppointmentById(appointmentId);

        // Only pending appointments can be declined
        if (!appointment.getStatus().equalsIgnoreCase("Pending")) {

            throw new IllegalArgumentException(
                    "Only pending appointments can be declined"
            );
        }

        appointment.setStatus("Declined");

        return appointment;
    }
}
