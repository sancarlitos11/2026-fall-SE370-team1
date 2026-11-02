
package com.autodetailflow.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

public class AvailabilityServiceTest {

    private final AvailabilityService service = new AvailabilityService();

    @Test
    public void testOverlappingAppointments() {

        LocalDateTime existing =
                LocalDateTime.of(2026, 11, 5, 10, 0);

        LocalDateTime requested =
                LocalDateTime.of(2026, 11, 5, 11, 0);

        boolean available = service.isTimeAvailable(
                requested, 120, existing, 120
        );

        assertFalse(available);
    }

    @Test
    public void testNonOverlappingAppointments() {

        LocalDateTime existing =
                LocalDateTime.of(2026, 11, 5, 10, 0);

        LocalDateTime requested =
                LocalDateTime.of(2026, 11, 5, 12, 0);

        boolean available = service.isTimeAvailable(
                requested, 120, existing, 120
        );

        assertTrue(available);
    }

    @Test
    public void testGetAvailableTimes() {

        LocalDateTime opening =
                LocalDateTime.of(2026, 11, 5, 9, 0);

        LocalDateTime closing =
                LocalDateTime.of(2026, 11, 5, 17, 0);

        List<LocalDateTime> existingStarts = Arrays.asList(
                LocalDateTime.of(2026, 11, 5, 11, 0)
        );

        List<Integer> existingDurations = Arrays.asList(120);

        List<LocalDateTime> availableTimes =
                service.getAvailableTimes(
                        opening,
                        closing,
                        120,
                        existingStarts,
                        existingDurations
                );

        assertEquals(3, availableTimes.size());

        assertEquals(
                LocalDateTime.of(2026, 11, 5, 9, 0),
                availableTimes.get(0)
        );

        assertEquals(
                LocalDateTime.of(2026, 11, 5, 13, 0),
                availableTimes.get(1)
        );

        assertEquals(
                LocalDateTime.of(2026, 11, 5, 15, 0),
                availableTimes.get(2)
        );
    }

    @Test
    public void testMultipleBookedAppointments() {

        LocalDateTime opening =
                LocalDateTime.of(2026, 11, 5, 9, 0);

        LocalDateTime closing =
                LocalDateTime.of(2026, 11, 5, 17, 0);

        List<LocalDateTime> existingStarts = Arrays.asList(
                LocalDateTime.of(2026, 11, 5, 9, 0),
                LocalDateTime.of(2026, 11, 5, 13, 0)
        );

        List<Integer> existingDurations = Arrays.asList(
                120, 120
        );

        List<LocalDateTime> availableTimes =
                service.getAvailableTimes(
                        opening,
                        closing,
                        120,
                        existingStarts,
                        existingDurations
                );

        assertEquals(2, availableTimes.size());

        assertEquals(
                LocalDateTime.of(2026, 11, 5, 11, 0),
                availableTimes.get(0)
        );

        assertEquals(
                LocalDateTime.of(2026, 11, 5, 15, 0),
                availableTimes.get(1)
        );
    }

    @Test
    public void testAppointmentStatuses() {

        assertTrue(service.blocksAvailability("Pending"));
        assertTrue(service.blocksAvailability("Scheduled"));

        assertFalse(service.blocksAvailability("Completed"));
        assertFalse(service.blocksAvailability("Cancelled"));
        assertFalse(service.blocksAvailability("Declined"));
        assertFalse(service.blocksAvailability(null));
    }
}
