package com.autodetailflow.service;

import com.autodetailflow.dao.InMemoryAppointmentDAO;
import com.autodetailflow.model.Appointment;
import com.autodetailflow.model.DetailingPackage;
import com.autodetailflow.model.User;
import com.autodetailflow.model.Vehicle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AppointmentManagerTest {

    private AppointmentManager appointmentManager;

    @BeforeEach
    void setUp() {
        InMemoryAppointmentDAO appointmentDAO = new InMemoryAppointmentDAO();
        AvailabilityService availabilityService = new AvailabilityService();

        appointmentManager = new AppointmentManager(
                appointmentDAO, availabilityService);
    }

  // Creates an appointment for testing
    private Appointment createTestAppointment(int appointmentId) {

        User user = new User(
                1, "Test Customer", "test@example.com",
                "5551234567", "Customer");

        Vehicle vehicle = new Vehicle(
                1, user, "Toyota", "Camry", 2020, "Sedan");

        DetailingPackage detailingPackage = new DetailingPackage(
                1, "Basic Detail", "Basic cleaning", 50.0, 60);

        return new Appointment(
                appointmentId,
                user,
                vehicle,
                detailingPackage,
                LocalDateTime.of(2026, 11, 10, 10, 0));
    }

     // Tests saving an appointment
    @Test
    void testSaveAppointment() {
        Appointment appointment = createTestAppointment(1);

        boolean saved = appointmentManager.saveAppointment(appointment);

        assertTrue(saved);
        assertNotNull(appointmentManager.getAppointment(1));
    }

    // Tests approving a pending appointment
    @Test
    void testApproveAppointment() {
        Appointment appointment = createTestAppointment(1);
        appointmentManager.saveAppointment(appointment);

        boolean approved = appointmentManager.approveAppointment(1);

        assertTrue(approved);
        assertEquals("Scheduled",
                appointmentManager.getAppointment(1).getStatus());
    }

    // Tests cancelling an appointment
    @Test
    void testCancelAppointment() {
        Appointment appointment = createTestAppointment(1);
        appointmentManager.saveAppointment(appointment);

        boolean cancelled = appointmentManager.cancelAppointment(1);

        assertTrue(cancelled);
        assertEquals("Cancelled",
                appointmentManager.getAppointment(1).getStatus());
    }
}