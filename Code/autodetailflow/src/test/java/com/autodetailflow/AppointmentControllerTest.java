
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

        // Address where mobile detailing will happen
        request.setServiceAddress(
                "123 Main Street, San Diego, CA 92101"
        );

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

    // Test 12: Employee can view all appointment details
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

        assertEquals(
                "Basic Detail",
                appointment.getDetailingPackage().getPackageName()
        );

        assertEquals(
                requestedTime,
                appointment.getAppointmentDateTime()
        );

        assertEquals(
                "Pending",
                appointment.getStatus()
        );
    }

    // Test 13: Employee confirms a pending appointment
    @Test
    public void testConfirmAppointment() {

        AppointmentController controller = createController();

        controller.createAppointment(
                createBookingRequest(futureDate(11))
        );

        ResponseEntity<Map<String, Object>> response =
                controller.confirmAppointment(1);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                "Confirmed",
                response.getBody().get("status")
        );

        assertEquals(
                "Confirmed",
                appointmentService.getAppointmentById(1).getStatus()
        );
    }

    // Test 14: Employee declines a pending appointment
    @Test
    public void testDeclineAppointment() {

        AppointmentController controller = createController();

        controller.createAppointment(
                createBookingRequest(futureDate(11))
        );

        ResponseEntity<Map<String, Object>> response =
                controller.declineAppointment(1);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                "Declined",
                response.getBody().get("status")
        );

        assertEquals(
                "Declined",
                appointmentService.getAppointmentById(1).getStatus()
        );
    }

    // Test 15: Cannot confirm an already confirmed appointment
    @Test
    public void testCannotConfirmTwice() {

        AppointmentController controller = createController();

        controller.createAppointment(
                createBookingRequest(futureDate(11))
        );

        controller.confirmAppointment(1);

        ResponseEntity<Map<String, Object>> response =
                controller.confirmAppointment(1);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                "Confirmed",
                appointmentService.getAppointmentById(1).getStatus()
        );
    }

    // Test 16: Cannot decline an already confirmed appointment
    @Test
    public void testCannotDeclineConfirmedAppointment() {

        AppointmentController controller = createController();

        controller.createAppointment(
                createBookingRequest(futureDate(11))
        );

        controller.confirmAppointment(1);

        ResponseEntity<Map<String, Object>> response =
                controller.declineAppointment(1);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                "Confirmed",
                appointmentService.getAppointmentById(1).getStatus()
        );
    }

    // Test 17: Cannot confirm an already declined appointment
    @Test
    public void testCannotConfirmDeclinedAppointment() {

        AppointmentController controller = createController();

        controller.createAppointment(
                createBookingRequest(futureDate(11))
        );

        controller.declineAppointment(1);

        ResponseEntity<Map<String, Object>> response =
                controller.confirmAppointment(1);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                "Declined",
                appointmentService.getAppointmentById(1).getStatus()
        );
    }

    // Test 18: Cannot confirm an appointment that does not exist
    @Test
    public void testConfirmNonexistentAppointment() {

        AppointmentController controller = createController();

        ResponseEntity<Map<String, Object>> response =
                controller.confirmAppointment(999);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertEquals(
                "Appointment not found",
                response.getBody().get("message")
        );
    }

    // Test 19: Cannot decline an appointment that does not exist
    @Test
    public void testDeclineNonexistentAppointment() {

        AppointmentController controller = createController();

        ResponseEntity<Map<String, Object>> response =
                controller.declineAppointment(999);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertEquals(
                "Appointment not found",
                response.getBody().get("message")
        );
    }

    // Test 20: Declining releases the appointment time
    @Test
    public void testDecliningReleasesTimeSlot() {

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

        // Decline the first appointment
        ResponseEntity<Map<String, Object>> declineResponse =
                controller.declineAppointment(1);

        assertEquals(
                HttpStatus.OK,
                declineResponse.getStatusCode()
        );

        // Another customer requests the same time
        BookingRequest second = createBookingRequest(
                futureDate(11)
        );

        second.setName("Jane Smith");
        second.setEmail("jane@example.com");

        ResponseEntity<Map<String, Object>> secondResponse =
                controller.createAppointment(second);

        assertEquals(
                HttpStatus.CREATED,
                secondResponse.getStatusCode()
        );

        assertEquals(
                2,
                appointmentService.getAllAppointments().size()
        );

        assertEquals(
                "Declined",
                appointmentService.getAppointmentById(1).getStatus()
        );

        assertEquals(
                "Pending",
                appointmentService.getAppointmentById(2).getStatus()
        );
    }
}
