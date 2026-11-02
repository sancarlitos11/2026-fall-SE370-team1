
package com.autodetailflow.service;

import com.autodetailflow.model.Appointment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    // Checks whether an appointment should block a time slot.
    public boolean blocksAvailability(String status) {
        if (status == null) {
            return false;
        }

        return status.equalsIgnoreCase("Pending")
                || status.equalsIgnoreCase("Scheduled");
    }

    // Checks whether two appointments overlap.
    public boolean isTimeAvailable(
            LocalDateTime requestedStart,
            int durationMinutes,
            LocalDateTime existingStart,
            int existingDurationMinutes) {

        LocalDateTime requestedEnd =
                requestedStart.plusMinutes(durationMinutes);

        LocalDateTime existingEnd =
                existingStart.plusMinutes(existingDurationMinutes);

        return !requestedStart.isBefore(existingEnd)
                || !requestedEnd.isAfter(existingStart);
    }

    // Generates available appointment times.
    public List<LocalDateTime> getAvailableTimes(
            LocalDateTime openingTime,
            LocalDateTime closingTime,
            int durationMinutes,
            List<LocalDateTime> existingStarts,
            List<Integer> existingDurations) {

        List<LocalDateTime> availableTimes = new ArrayList<>();

        if (durationMinutes <= 0 ||
                existingStarts.size() != existingDurations.size()) {
            throw new IllegalArgumentException("Invalid appointment data");
        }

        LocalDateTime currentTime = openingTime;

        while (!currentTime.plusMinutes(durationMinutes).isAfter(closingTime)) {

            boolean available = true;

            for (int i = 0; i < existingStarts.size(); i++) {

                if (!isTimeAvailable(
                        currentTime,
                        durationMinutes,
                        existingStarts.get(i),
                        existingDurations.get(i))) {

                    available = false;
                    break;
                }
            }

            if (available) {
                availableTimes.add(currentTime);
            }

            currentTime = currentTime.plusMinutes(durationMinutes);
        }

        return availableTimes;
    }

    // Generates available times using actual Appointment objects.
    public List<LocalDateTime> getAvailableTimesForAppointments(
            LocalDateTime openingTime,
            LocalDateTime closingTime,
            int durationMinutes,
            List<Appointment> appointments) {

        List<LocalDateTime> existingStarts = new ArrayList<>();
        List<Integer> existingDurations = new ArrayList<>();

        for (Appointment appointment : appointments) {

            // Pending and Scheduled appointments reserve their time slots.
            if (blocksAvailability(appointment.getStatus())) {

                existingStarts.add(appointment.getAppointmentDateTime());

                existingDurations.add(
                        appointment.getDetailingPackage().getPackageDuration()
                );
            }
        }

        // Reuse our existing availability calculation.
        return getAvailableTimes(
                openingTime,
                closingTime,
                durationMinutes,
                existingStarts,
                existingDurations
        );
    }
}
