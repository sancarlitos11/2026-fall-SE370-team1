
package com.autodetailflow;

import com.autodetailflow.controller.AppointmentController;
import com.autodetailflow.controller.AppointmentController.BookingRequest;
import com.autodetailflow.model.Appointment;
import com.autodetailflow.service.AppointmentService;
import com.autodetailflow.service.AvailabilityService;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    // Creates a future appointment date
    private LocalDateTime futureDate(int hour) {
        return LocalDate.now().plusDays(1).atTime(hour, 0);
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
                futureDate(11)
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
                futureDate(11)
        );

        ResponseEntity<Map<String, Object>> firstResponse =
                controller.createAppointment(first);

        assertEquals(
                HttpStatus.CREATED,
                firstResponse.getStatusCode()
        );

        BookingRequest second = createBookingRequest(
                futureDate(12)
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
                futureDate(11)
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
                futureDate(11)
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
                futureDate(7)
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
                futureDate(16)
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
                futureDate(15)
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

    // Test 9: Employee can retrieve an empty list
    @Test
    public void testGetAppointmentsWhenEmpty() {

        AppointmentController controller = createController();

        ResponseEntity<List<Appointment>> response =
                controller.getAppointments();

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(response.getBody().isEmpty());
    }

    // Test 10: Employee can retrieve a submitted request
    @Test
    public void testGetSubmittedAppointment() {

        AppointmentController controller = createController();

        BookingRequest request = createBookingRequest(
                futureDate(11)
        );

        ResponseEntity<Map<String, Object>> bookingResponse =
                controller.createAppointment(request);

        assertEquals(
                HttpStatus.CREATED,
                bookingResponse.getStatusCode()
        );

        ResponseEntity<List<Appointment>> response =
                controller.getAppointments();

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(1, response.getBody().size());

        Appointment appointment = response.getBody().get(0);

        assertEquals("Pending", appointment.getStatus());

        assertEquals(
                futureDate(11),
                appointment.getAppointmentDateTime()
        );
    }

    // Test 11: Employee can retrieve multiple requests
    @Test
    public void testGetMultipleAppointments() {

        AppointmentController controller = createController();

        BookingRequest first = createBookingRequest(
                futureDate(9)
        );

        BookingRequest second = createBookingRequest(
                futureDate(13)
        );

        second.setName("Jane Smith");

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(first).getStatusCode()
        );

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(second).getStatusCode()
        );

        ResponseEntity<List<Appointment>> response =
                controller.getAppointments();

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(2, response.getBody().size());

        assertEquals(
                "Carlos Garcia",
                response.getBody().get(0).getUser().getName()
        );

        assertEquals(
                "Jane Smith",
                response.getBody().get(1).getUser().getName()
        );
    }

    // Test 12: Employee can view customer, vehicle,
    // service, and appointment information
    @Test
    public void testAppointmentDetailsPreserved() {

        AppointmentController controller = createController();

        LocalDateTime requestedTime = futureDate(11);

        BookingRequest request = createBookingRequest(
                requestedTime
        );

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(request).getStatusCode()
        );

        ResponseEntity<List<Appointment>> response =
                controller.getAppointments();

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        Appointment appointment = response.getBody().get(0);

        // Customer information
        assertEquals(
                "Carlos Garcia",
                appointment.getUser().getName()
        );

        assertEquals(
                "carlos@example.com",
                appointment.getUser().getEmail()
        );

        assertEquals(
                "6195551234",
                appointment.getUser().getPhoneNumber()
        );

        // Vehicle information
        assertEquals(
                2017,
                appointment.getVehicle().getYear()
        );

        assertEquals(
                "Nissan",
                appointment.getVehicle().getMake()
        );

        assertEquals(
                "Altima",
                appointment.getVehicle().getModel()
        );

        assertEquals(
                "Sedan",
                appointment.getVehicle().getType()
        );

        // Selected detailing service
        assertEquals(
                "Basic Detail",
                appointment.getDetailingPackage().getPackageName()
        );

        // Requested date and time
        assertEquals(
                requestedTime,
                appointment.getAppointmentDateTime()
        );

        // New requests must be Pending
        assertEquals(
                "Pending",
                appointment.getStatus()
        );
    }
}
