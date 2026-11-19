
package com.autodetailflow;

import com.autodetailflow.controller.AvailabilityController;
import com.autodetailflow.model.Appointment;
import com.autodetailflow.model.DetailingPackage;
import com.autodetailflow.service.AppointmentService;
import com.autodetailflow.service.AvailabilityService;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AvailabilityControllerTest {

    private final LocalDate date = LocalDate.of(2026, 11, 19);

    private AvailabilityService availabilityService;
    private AppointmentService appointmentService;
    private AvailabilityController controller;

    // Sets up the services for each test
    private void setup() {
        availabilityService = new AvailabilityService();
        appointmentService =
                new AppointmentService(availabilityService);

        controller = new AvailabilityController(
                availabilityService,
                appointmentService
        );
    }

    // Creates an appointment for testing
    private Appointment createAppointment(
            int id,
            int hour,
            int duration) {

        DetailingPackage detailingPackage = new DetailingPackage(
                id,
                "Detail Package",
                "Car detailing service",
                100.00,
                duration
        );

        return new Appointment(
                id,
                null,
                null,
                detailingPackage,
                date.atTime(hour, 0)
        );
    }

    @Test
    public void testAppointmentBlocksAvailableTime() {
        setup();

        Appointment appointment =
                createAppointment(1, 11, 120);

        appointmentService.addAppointment(appointment);

        List<LocalDateTime> times =
                controller.getAvailableTimes(date, 120);

        assertFalse(times.contains(date.atTime(11, 0)));
    }

    @Test
    public void testCancelledAppointmentReleasesTime() {
        setup();

        Appointment appointment =
                createAppointment(1, 11, 120);

        appointment.setStatus("Cancelled");
        appointmentService.addAppointment(appointment);

        List<LocalDateTime> times =
                controller.getAvailableTimes(date, 120);

        assertTrue(times.contains(date.atTime(11, 0)));
    }

    @Test
    public void testDeclinedAppointmentReleasesTime() {
        setup();

        Appointment appointment =
                createAppointment(1, 11, 120);

        appointment.setStatus("Declined");
        appointmentService.addAppointment(appointment);

        List<LocalDateTime> times =
                controller.getAvailableTimes(date, 120);

        assertTrue(times.contains(date.atTime(11, 0)));
    }

    @Test
    public void testConfirmedAppointmentBlocksTime() {
        setup();

        Appointment appointment =
                createAppointment(1, 11, 120);

        appointment.setStatus("Confirmed");
        appointmentService.addAppointment(appointment);

        List<LocalDateTime> times =
                controller.getAvailableTimes(date, 120);

        assertFalse(times.contains(date.atTime(11, 0)));
    }

    @Test
    public void testOverlappingAppointments() {
        setup();

        Appointment appointment =
                createAppointment(1, 10, 120);

        appointmentService.addAppointment(appointment);

        List<LocalDateTime> times =
                controller.getAvailableTimes(date, 120);

        assertFalse(times.contains(date.atTime(9, 0)));
        assertFalse(times.contains(date.atTime(11, 0)));
        assertTrue(times.contains(date.atTime(13, 0)));
    }

    @Test
    public void testDifferentServiceDurations() {
        setup();

        Appointment appointment =
                createAppointment(1, 11, 180);

        appointmentService.addAppointment(appointment);

        List<LocalDateTime> times =
                controller.getAvailableTimes(date, 120);

        assertTrue(times.contains(date.atTime(9, 0)));
        assertFalse(times.contains(date.atTime(11, 0)));
        assertFalse(times.contains(date.atTime(13, 0)));
        assertTrue(times.contains(date.atTime(15, 0)));
    }

    @Test
    public void testPreventDoubleBooking() {
        setup();

        // First appointment: 11 AM to 1 PM
        Appointment first =
                createAppointment(1, 11, 120);

        appointmentService.addAppointment(first);

        // Second appointment: 12 PM to 2 PM
        Appointment second =
                createAppointment(2, 12, 120);

        // The system should reject the second appointment
        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.addAppointment(second)
        );

        // Only the first appointment should be saved
        assertEquals(
                1,
                appointmentService.getAllAppointments().size()
        );
    }

    @Test
    public void testBackToBackAppointmentsAllowed() {
        setup();

        // First appointment: 11 AM to 1 PM
        Appointment first =
                createAppointment(1, 11, 120);

        // Second appointment: 1 PM to 3 PM
        Appointment second =
                createAppointment(2, 13, 120);

        appointmentService.addAppointment(first);
        appointmentService.addAppointment(second);

        // Both appointments should be saved
        assertEquals(
                2,
                appointmentService.getAllAppointments().size()
        );
    }

    @Test
    public void testCancelledAppointmentAllowsNewBooking() {
        setup();

        Appointment first =
                createAppointment(1, 11, 120);

        first.setStatus("Cancelled");
        appointmentService.addAppointment(first);

        Appointment second =
                createAppointment(2, 11, 120);

        // New booking should be allowed
        appointmentService.addAppointment(second);

        assertEquals(
                2,
                appointmentService.getAllAppointments().size()
        );
    }
}
