
package com.autodetailflow;

import com.autodetailflow.controller.AppointmentController;
import com.autodetailflow.controller.AppointmentController.BookingRequest;
import com.autodetailflow.service.AppointmentService;
import com.autodetailflow.service.AvailabilityService;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

public class AppointmentControllerTest {

    // Creates a valid customer booking request
    private BookingRequest createBookingRequest(
            LocalDateTime dateTime) {

        BookingRequest request = new BookingRequest();

        request.setName("Carlos Garcia");
        request.setEmail("carlos@example.com");
        request.setPhoneNumber("6195551234");

        request.setMake("Nissan");
        request.setModel("Altima");
        request.setYear(2017);
        request.setType("Sedan");

        request.setPackageId(1);
        request.setAppointmentDateTime(dateTime);

        return request;
    }

    // Creates a controller and service for testing
    private AppointmentService appointmentService;

    private AppointmentController createController() {

        AvailabilityService availabilityService =
                new AvailabilityService();

        appointmentService =
                new AppointmentService(availabilityService);

        return new AppointmentController(appointmentService);
    }

    // Test 1: Successful appointment request
    @Test
    public void testSuccessfulAppointmentRequest() {

        AppointmentController controller = createController();

        BookingRequest request = createBookingRequest(
                LocalDateTime.of(2026, 12, 20, 11, 0)
        );

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertEquals(
                "Pending",
                response.getBody().get("status")
        );

        assertEquals(
                1,
                appointmentService.getAllAppointments().size()
        );
    }

    // Test 2: Prevent double-booking
    @Test
    public void testDoubleBookingRejected() {

        AppointmentController controller = createController();

        BookingRequest first = createBookingRequest(
                LocalDateTime.of(2026, 12, 20, 11, 0)
        );

        ResponseEntity<Map<String, Object>> firstResponse =
                controller.createAppointment(first);

        assertEquals(
                HttpStatus.CREATED,
                firstResponse.getStatusCode()
        );

        BookingRequest second = createBookingRequest(
                LocalDateTime.of(2026, 12, 20, 12, 0)
        );

        ResponseEntity<Map<String, Object>> secondResponse =
                controller.createAppointment(second);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                secondResponse.getStatusCode()
        );

        assertEquals(
                1,
                appointmentService.getAllAppointments().size()
        );
    }

    // Test 3: Reject missing customer information
    @Test
    public void testMissingCustomerInformation() {

        AppointmentController controller = createController();

        BookingRequest request = createBookingRequest(
                LocalDateTime.of(2026, 12, 20, 11, 0)
        );

        request.setName("");

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                0,
                appointmentService.getAllAppointments().size()
        );
    }

    // Test 4: Reject invalid detailing package
    @Test
    public void testInvalidDetailingPackage() {

        AppointmentController controller = createController();

        BookingRequest request = createBookingRequest(
                LocalDateTime.of(2026, 12, 20, 11, 0)
        );

        request.setPackageId(99);

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                0,
                appointmentService.getAllAppointments().size()
        );
    }

    // Test 5: Reject appointment in the past
    @Test
    public void testPastAppointmentRejected() {

        AppointmentController controller = createController();

        BookingRequest request = createBookingRequest(
                LocalDateTime.now().minusDays(1)
        );

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                0,
                appointmentService.getAllAppointments().size()
        );
    }

    // Test 6: Reject appointment before opening
    @Test
    public void testBeforeOpeningRejected() {

        AppointmentController controller = createController();

        BookingRequest request = createBookingRequest(
                LocalDateTime.now()
                        .plusDays(1)
                        .toLocalDate()
                        .atTime(7, 0)
        );

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                0,
                appointmentService.getAllAppointments().size()
        );
    }

    // Test 7: Reject appointment ending after closing
    @Test
    public void testAfterClosingRejected() {

        AppointmentController controller = createController();

        BookingRequest request = createBookingRequest(
                LocalDateTime.now()
                        .plusDays(1)
                        .toLocalDate()
                        .atTime(16, 0)
        );

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                0,
                appointmentService.getAllAppointments().size()
        );
    }

    // Test 8: Allow appointment ending at closing
    @Test
    public void testAppointmentAtThreePMAllowed() {

        AppointmentController controller = createController();

        BookingRequest request = createBookingRequest(
                LocalDateTime.now()
                        .plusDays(1)
                        .toLocalDate()
                        .atTime(15, 0)
        );

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertEquals(
                "Pending",
                response.getBody().get("status")
        );

        assertEquals(
                1,
                appointmentService.getAllAppointments().size()
        );
    }
}

