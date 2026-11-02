
package com.autodetailflow.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

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
}
