
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

    public AppointmentService(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    // Returns all appointments
    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments);
    }

    // Adds a new appointment if the time is available
    public synchronized void addAppointment(Appointment appointment) {

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
                appointment.getDetailingPackage().getPackageDuration();

        if (requestedDuration <= 0) {
            throw new IllegalArgumentException(
                    "Appointment duration must be positive"
            );
        }

        // Only active appointments reserve time
        if (availabilityService.blocksAvailability(
                appointment.getStatus())) {

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
}
