
package com.autodetailflow.service;

import com.autodetailflow.model.Appointment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    // Stores time periods blocked by employees.
    private final List<BlockedTime> blockedTimes = new ArrayList<>();

    // Represents a blocked period of time.
    private static class BlockedTime {
        private final LocalDateTime start;
        private final LocalDateTime end;

        public BlockedTime(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }

    // Checks whether an appointment should block a time slot.
    public boolean blocksAvailability(String status) {
        if (status == null) {
            return false;
        }

        return status.equalsIgnoreCase("Pending")
                || status.equalsIgnoreCase("Scheduled")
                || status.equalsIgnoreCase("Confirmed");
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

    // Adds an unavailable time period.
    public synchronized void blockTime(
            LocalDateTime start,
            LocalDateTime end) {

        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "Blocked time must have a valid start and end"
            );
        }

        blockedTimes.add(new BlockedTime(start, end));
    }

    // Removes a previously blocked time period.
    public synchronized boolean unblockTime(
            LocalDateTime start,
            LocalDateTime end) {

        return blockedTimes.removeIf(block ->
                block.start.equals(start)
                        && block.end.equals(end)
        );
    }

    // Checks whether a requested appointment overlaps blocked time.
    public synchronized boolean isBlocked(
            LocalDateTime requestedStart,
            int durationMinutes) {

        if (requestedStart == null || durationMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Invalid requested appointment time"
            );
        }

        LocalDateTime requestedEnd =
                requestedStart.plusMinutes(durationMinutes);

        for (BlockedTime block : blockedTimes) {
            if (requestedStart.isBefore(block.end)
                    && requestedEnd.isAfter(block.start)) {
                return true;
            }
        }

        return false;
    }

    // Generates available appointment times.
    public List<LocalDateTime> getAvailableTimes(
            LocalDateTime openingTime,
            LocalDateTime closingTime,
            int durationMinutes,
            List<LocalDateTime> existingStarts,
            List<Integer> existingDurations) {

        List<LocalDateTime> availableTimes = new ArrayList<>();

        if (durationMinutes <= 0
                || existingStarts.size() != existingDurations.size()) {
            throw new IllegalArgumentException(
                    "Invalid appointment data"
            );
        }

        LocalDateTime currentTime = openingTime;

        while (!currentTime.plusMinutes(durationMinutes)
                .isAfter(closingTime)) {

            boolean available = true;

            // Check existing appointments.
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

            // Check employee-blocked periods.
            if (available && isBlocked(currentTime, durationMinutes)) {
                available = false;
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

            if (blocksAvailability(appointment.getStatus())) {

                existingStarts.add(
                        appointment.getAppointmentDateTime()
                );

                existingDurations.add(
                        appointment.getDetailingPackage()
                                .getPackageDuration()
                );
            }
        }

        return getAvailableTimes(
                openingTime,
                closingTime,
                durationMinutes,
                existingStarts,
                existingDurations
        );
    }
}
