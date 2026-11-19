
package com.autodetailflow;

import com.autodetailflow.controller.AppointmentController;
import com.autodetailflow.controller.AppointmentController.BookingRequest;
import com.autodetailflow.model.Appointment;
import com.autodetailflow.model.DetailingPackage;
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

    private AppointmentService appointmentService;

    // Create a valid booking request
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

        request.setServiceAddress(
                "123 Main Street, San Diego, CA 92101"
        );

        // Full Interior: 2 hours
        request.setPackageId(2);
        request.setAppointmentDateTime(dateTime);

        return request;
    }

    private LocalDateTime futureDate(int hour) {
        return LocalDate.now().plusDays(1).atTime(hour, 0);
    }

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

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(
                        createBookingRequest(futureDate(11))
                );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Pending", response.getBody().get("status"));
        assertEquals(1, appointmentService.getAllAppointments().size());
    }

    // Test 2: Prevent double-booking
    @Test
    public void testDoubleBookingRejected() {

        AppointmentController controller = createController();

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(
                        createBookingRequest(futureDate(11))
                ).getStatusCode()
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.createAppointment(
                        createBookingRequest(futureDate(12))
                ).getStatusCode()
        );

        assertEquals(1, appointmentService.getAllAppointments().size());
    }

    // Test 3: Reject missing customer information
    @Test
    public void testMissingCustomerInformation() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(futureDate(11));

        request.setName("");

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.createAppointment(request).getStatusCode()
        );

        assertTrue(appointmentService.getAllAppointments().isEmpty());
    }

    // Test 4: Reject invalid detailing package
    @Test
    public void testInvalidDetailingPackage() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(futureDate(11));

        request.setPackageId(99);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.createAppointment(request).getStatusCode()
        );

        assertTrue(appointmentService.getAllAppointments().isEmpty());
    }

    // Test 5: Reject appointment in the past
    @Test
    public void testPastAppointmentRejected() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(
                        LocalDateTime.now().minusDays(1)
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.createAppointment(request).getStatusCode()
        );

        assertTrue(appointmentService.getAllAppointments().isEmpty());
    }

    // Test 6: Reject appointment before opening
    @Test
    public void testBeforeOpeningRejected() {

        AppointmentController controller = createController();

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.createAppointment(
                        createBookingRequest(futureDate(7))
                ).getStatusCode()
        );
    }

    // Test 7: Reject appointment ending after closing
    @Test
    public void testAfterClosingRejected() {

        AppointmentController controller = createController();

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.createAppointment(
                        createBookingRequest(futureDate(16))
                ).getStatusCode()
        );
    }

    // Test 8: Allow appointment ending exactly at closing
    @Test
    public void testAppointmentAtThreePMAllowed() {

        AppointmentController controller = createController();

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(
                        createBookingRequest(futureDate(15))
                ).getStatusCode()
        );
    }

    // Test 9: Employee can retrieve an empty list
    @Test
    public void testGetAppointmentsWhenEmpty() {

        AppointmentController controller = createController();

        ResponseEntity<List<Appointment>> response =
                controller.getAppointments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // Test 10: Employee can retrieve a submitted request
    @Test
    public void testGetSubmittedAppointment() {

        AppointmentController controller = createController();

        controller.createAppointment(
                createBookingRequest(futureDate(11))
        );

        ResponseEntity<List<Appointment>> response =
                controller.getAppointments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
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

        BookingRequest first =
                createBookingRequest(futureDate(9));

        BookingRequest second =
                createBookingRequest(futureDate(13));

        second.setName("Jane Smith");

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(first).getStatusCode()
        );

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(second).getStatusCode()
        );

        List<Appointment> appointments =
                controller.getAppointments().getBody();

        assertNotNull(appointments);
        assertEquals(2, appointments.size());

        assertEquals(
                "Carlos Garcia",
                appointments.get(0).getUser().getName()
        );

        assertEquals(
                "Jane Smith",
                appointments.get(1).getUser().getName()
        );
    }

    // Test 12: Employee can view all appointment details
    @Test
    public void testAppointmentDetailsPreserved() {

        AppointmentController controller = createController();

        LocalDateTime requestedTime = futureDate(11);

        controller.createAppointment(
                createBookingRequest(requestedTime)
        );

        Appointment appointment =
                controller.getAppointments().getBody().get(0);

        assertEquals("Carlos Garcia", appointment.getUser().getName());
        assertEquals("carlos@example.com", appointment.getUser().getEmail());
        assertEquals("6195551234", appointment.getUser().getPhoneNumber());

        assertEquals(2017, appointment.getVehicle().getYear());
        assertEquals("Nissan", appointment.getVehicle().getMake());
        assertEquals("Altima", appointment.getVehicle().getModel());
        assertEquals("Sedan", appointment.getVehicle().getType());

        assertEquals(
                "Full Interior",
                appointment.getDetailingPackage().getPackageName()
        );

        assertEquals(
                179.00,
                appointment.getDetailingPackage().getPackagePrice(),
                0.001
        );

        assertEquals(
                "123 Main Street, San Diego, CA 92101",
                appointment.getServiceAddress()
        );

        assertEquals(requestedTime, appointment.getAppointmentDateTime());
        assertEquals("Pending", appointment.getStatus());
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

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Confirmed", response.getBody().get("status"));

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

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Declined", response.getBody().get("status"));
    }

    // Test 15: Cannot confirm twice
    @Test
    public void testCannotConfirmTwice() {

        AppointmentController controller = createController();

        controller.createAppointment(
                createBookingRequest(futureDate(11))
        );

        controller.confirmAppointment(1);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.confirmAppointment(1).getStatusCode()
        );
    }

    // Test 16: Cannot decline a confirmed appointment
    @Test
    public void testCannotDeclineConfirmedAppointment() {

        AppointmentController controller = createController();

        controller.createAppointment(
                createBookingRequest(futureDate(11))
        );

        controller.confirmAppointment(1);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.declineAppointment(1).getStatusCode()
        );
    }

    // Test 17: Cannot confirm a declined appointment
    @Test
    public void testCannotConfirmDeclinedAppointment() {

        AppointmentController controller = createController();

        controller.createAppointment(
                createBookingRequest(futureDate(11))
        );

        controller.declineAppointment(1);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.confirmAppointment(1).getStatusCode()
        );
    }

    // Test 18: Cannot confirm nonexistent appointment
    @Test
    public void testConfirmNonexistentAppointment() {

        AppointmentController controller = createController();

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.confirmAppointment(999).getStatusCode()
        );
    }

    // Test 19: Cannot decline nonexistent appointment
    @Test
    public void testDeclineNonexistentAppointment() {

        AppointmentController controller = createController();

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.declineAppointment(999).getStatusCode()
        );
    }

    // Test 20: Declining releases the appointment time
    @Test
    public void testDecliningReleasesTimeSlot() {

        AppointmentController controller = createController();

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(
                        createBookingRequest(futureDate(11))
                ).getStatusCode()
        );

        controller.declineAppointment(1);

        BookingRequest second =
                createBookingRequest(futureDate(11));

        second.setName("Jane Smith");

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(second).getStatusCode()
        );

        assertEquals(2, appointmentService.getAllAppointments().size());
        assertEquals(
                "Declined",
                appointmentService.getAppointmentById(1).getStatus()
        );
        assertEquals(
                "Pending",
                appointmentService.getAppointmentById(2).getStatus()
        );
    }

    // Test 21: All six packages are available
    @Test
    public void testSixDetailingPackagesAvailable() {

        AppointmentController controller = createController();

        ResponseEntity<List<DetailingPackage>> response =
                controller.getPackages();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(6, response.getBody().size());

        assertEquals(
                "Ceramic Package",
                response.getBody().get(0).getPackageName()
        );

        assertEquals(
                "Deluxe Package",
                response.getBody().get(5).getPackageName()
        );
    }

    // Test 22: Every package can be booked
    @Test
    public void testAllSixPackagesCanBeBooked() {

        for (int packageId = 1; packageId <= 6; packageId++) {

            AppointmentController controller = createController();

            BookingRequest request =
                    createBookingRequest(futureDate(11));

            request.setPackageId(packageId);

            ResponseEntity<Map<String, Object>> response =
                    controller.createAppointment(request);

            assertEquals(
                    HttpStatus.CREATED,
                    response.getStatusCode(),
                    "Package " + packageId + " should be bookable"
            );

            assertEquals(
                    packageId,
                    response.getBody().get("packageId")
            );
        }
    }

    // Test 23: Coupe receives sedan pricing
    @Test
    public void testCoupeUsesSedanPricing() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(futureDate(11));

        request.setType("Coupe");
        request.setPackageId(4);

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(279.00, (Double) response.getBody().get("price"), 0.001);
    }

    // Test 24: Small SUV receives correct pricing
    @Test
    public void testSmallSUVPricing() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(futureDate(11));

        request.setType("Small SUV");
        request.setPackageId(4);

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(289.00, (Double) response.getBody().get("price"), 0.001);
    }

    // Test 25: Truck receives large SUV pricing
    @Test
    public void testTruckUsesLargeSUVPricing() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(futureDate(11));

        request.setType("Truck");
        request.setPackageId(4);

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(319.00, (Double) response.getBody().get("price"), 0.001);
    }

    // Test 26: Van receives minivan pricing
    @Test
    public void testVanPricing() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(futureDate(11));

        request.setType("Van");
        request.setPackageId(4);

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(349.00, (Double) response.getBody().get("price"), 0.001);
    }

    // Test 27: Ceramic package lasts four hours
    @Test
    public void testCeramicPackageDuration() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(futureDate(9));

        request.setPackageId(1);

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(240, response.getBody().get("duration"));
    }

    // Test 28: Ceramic package cannot end after closing
    @Test
    public void testCeramicPackageAfterClosingRejected() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(futureDate(14));

        request.setPackageId(1);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.createAppointment(request).getStatusCode()
        );
    }

    // Test 29: Ceramic package unavailable for vans
    @Test
    public void testCeramicPackageUnavailableForVan() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(futureDate(9));

        request.setPackageId(1);
        request.setType("Van");

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(appointmentService.getAllAppointments().isEmpty());
    }

    // Test 30: Reject unknown vehicle category
    @Test
    public void testInvalidVehicleTypeRejected() {

        AppointmentController controller = createController();

        BookingRequest request =
                createBookingRequest(futureDate(11));

        request.setType("Motorcycle");

        ResponseEntity<Map<String, Object>> response =
                controller.createAppointment(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(appointmentService.getAllAppointments().isEmpty());
    }

    // Test 31: Prevent overlapping packages of different durations
    @Test
    public void testDifferentPackageDurationsCannotOverlap() {

        AppointmentController controller = createController();

        BookingRequest ceramic =
                createBookingRequest(futureDate(9));

        ceramic.setPackageId(1);

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(ceramic).getStatusCode()
        );

        BookingRequest interior =
                createBookingRequest(futureDate(12));

        interior.setPackageId(2);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                controller.createAppointment(interior).getStatusCode()
        );

        assertEquals(1, appointmentService.getAllAppointments().size());
    }

    // Test 32: A package can be booked after another one ends
    @Test
    public void testBookingAfterCeramicPackageEnds() {

        AppointmentController controller = createController();

        BookingRequest ceramic =
                createBookingRequest(futureDate(9));

        ceramic.setPackageId(1);

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(ceramic).getStatusCode()
        );

        BookingRequest interior =
                createBookingRequest(futureDate(13));

        interior.setPackageId(2);

        assertEquals(
                HttpStatus.CREATED,
                controller.createAppointment(interior).getStatusCode()
        );

        assertEquals(2, appointmentService.getAllAppointments().size());
    }
}
