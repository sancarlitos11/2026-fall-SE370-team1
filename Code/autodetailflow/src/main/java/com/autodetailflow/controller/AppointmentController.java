
package com.autodetailflow.controller;

import com.autodetailflow.model.Appointment;
import com.autodetailflow.model.User;
import com.autodetailflow.model.Vehicle;
import com.autodetailflow.model.DetailingPackage;
import com.autodetailflow.service.AppointmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AtomicInteger nextId = new AtomicInteger(1);

    public AppointmentController(
            AppointmentService appointmentService) {

        this.appointmentService = appointmentService;
    }

    // Returns all appointment requests for employees
    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments() {

        List<Appointment> appointments =
                appointmentService.getAllAppointments();

        return ResponseEntity.ok(appointments);
    }

    // Returns the available detailing packages
    @GetMapping("/packages")
    public ResponseEntity<List<DetailingPackage>> getPackages() {

        List<DetailingPackage> packages = new ArrayList<>();

        for (int id = 1; id <= 6; id++) {
            packages.add(createPackage(id));
        }

        return ResponseEntity.ok(packages);
    }

    // Creates one of the six detailing packages
    private DetailingPackage createPackage(int packageId) {

        switch (packageId) {

            case 1:
                return new DetailingPackage(
                        1,
                        "Ceramic Package",
                        "Ceramic detailing package",
                        199.00,
                        240
                );

            case 2:
                return new DetailingPackage(
                        2,
                        "Full Interior",
                        "Complete interior detailing",
                        179.00,
                        120
                );

            case 3:
                return new DetailingPackage(
                        3,
                        "Full Exterior",
                        "Complete exterior detailing",
                        129.00,
                        120
                );

            case 4:
                return new DetailingPackage(
                        4,
                        "Elite Package",
                        "Elite detailing service",
                        279.00,
                        120
                );

            case 5:
                return new DetailingPackage(
                        5,
                        "Premium Package",
                        "Premium detailing service",
                        199.00,
                        120
                );

            case 6:
                return new DetailingPackage(
                        6,
                        "Deluxe Package",
                        "Deluxe detailing service",
                        169.00,
                        120
                );

            default:
                throw new IllegalArgumentException(
                        "Invalid detailing package"
                );
        }
    }

    // Confirms a pending appointment request
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmAppointment(
            @PathVariable int id) {

        Map<String, Object> response = new HashMap<>();

        try {
            Appointment appointment =
                    appointmentService.confirmAppointment(id);

            response.put(
                    "message",
                    "Appointment confirmed successfully"
            );

            response.put(
                    "appointmentId",
                    appointment.getAppointmentId()
            );

            response.put(
                    "status",
                    appointment.getStatus()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException error) {

            response.put("message", error.getMessage());

            if ("Appointment not found".equals(error.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    // Declines a pending appointment request
    @PatchMapping("/{id}/decline")
    public ResponseEntity<Map<String, Object>> declineAppointment(
            @PathVariable int id) {

        Map<String, Object> response = new HashMap<>();

        try {
            Appointment appointment =
                    appointmentService.declineAppointment(id);

            response.put(
                    "message",
                    "Appointment declined successfully"
            );

            response.put(
                    "appointmentId",
                    appointment.getAppointmentId()
            );

            response.put(
                    "status",
                    appointment.getStatus()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException error) {

            response.put("message", error.getMessage());

            if ("Appointment not found".equals(error.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    // Receives a new appointment request
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(
            @RequestBody BookingRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate customer, vehicle, and service address
            if (request == null ||
                    isBlank(request.getName()) ||
                    isBlank(request.getEmail()) ||
                    isBlank(request.getPhoneNumber()) ||
                    isBlank(request.getMake()) ||
                    isBlank(request.getModel()) ||
                    isBlank(request.getType()) ||
                    isBlank(request.getServiceAddress()) ||
                    request.getYear() < 1900 ||
                    request.getAppointmentDateTime() == null) {

                throw new IllegalArgumentException(
                        "Please complete all required fields"
                );
            }

            // Get the customer's selected detailing package
            DetailingPackage selectedPackage =
                    createPackage(request.getPackageId());

            // Calculate the price using the vehicle category
            double calculatedPrice =
                    selectedPackage.calculatePrice(
                            request.getType()
                    );

            // Create the package with the calculated price
            DetailingPackage detailingPackage =
                    new DetailingPackage(
                            selectedPackage.getPackageId(),
                            selectedPackage.getPackageName(),
                            selectedPackage.getPackageDescription(),
                            calculatedPrice,
                            selectedPackage.getPackageDuration()
                    );

            LocalDateTime appointmentTime =
                    request.getAppointmentDateTime();

            // Do not allow appointments in the past
            if (appointmentTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException(
                        "Cannot book an appointment in the past"
                );
            }

            // Business hours: 9:00 AM to 5:00 PM
            LocalDateTime openingTime =
                    appointmentTime.toLocalDate().atTime(9, 0);

            LocalDateTime closingTime =
                    appointmentTime.toLocalDate().atTime(17, 0);

            LocalDateTime appointmentEnd =
                    appointmentTime.plusMinutes(
                            detailingPackage.getPackageDuration()
                    );

            // Appointment must fit within business hours
            if (appointmentTime.isBefore(openingTime) ||
                    appointmentEnd.isAfter(closingTime)) {

                throw new IllegalArgumentException(
                        "Appointment must be within business hours"
                );
            }

            int id = nextId.getAndIncrement();

            User customer = new User(
                    id,
                    request.getName(),
                    request.getEmail(),
                    request.getPhoneNumber(),
                    "Customer"
            );

            Vehicle vehicle = new Vehicle(
                    id,
                    customer,
                    request.getMake(),
                    request.getModel(),
                    request.getYear(),
                    request.getType()
            );

            // Create appointment with service and address
            Appointment appointment = new Appointment(
                    id,
                    customer,
                    vehicle,
                    detailingPackage,
                    appointmentTime,
                    request.getServiceAddress().trim()
            );

            // Reject overlapping appointment times
            appointmentService.addAppointment(appointment);

            response.put(
                    "message",
                    "Appointment request submitted successfully"
            );

            response.put(
                    "appointmentId",
                    appointment.getAppointmentId()
            );

            response.put(
                    "status",
                    appointment.getStatus()
            );

            response.put(
                    "serviceAddress",
                    appointment.getServiceAddress()
            );

            response.put(
                    "packageId",
                    detailingPackage.getPackageId()
            );

            response.put(
                    "packageName",
                    detailingPackage.getPackageName()
            );

            response.put(
                    "price",
                    detailingPackage.getPackagePrice()
            );

            response.put(
                    "duration",
                    detailingPackage.getPackageDuration()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException error) {

            response.put("message", error.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Information received from the customer booking form
    public static class BookingRequest {

        private String name;
        private String email;
        private String phoneNumber;

        private String make;
        private String model;
        private int year;
        private String type;

        private String serviceAddress;

        private int packageId;
        private LocalDateTime appointmentDateTime;

        public BookingRequest() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getMake() {
            return make;
        }

        public void setMake(String make) {
            this.make = make;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getServiceAddress() {
            return serviceAddress;
        }

        public void setServiceAddress(String serviceAddress) {
            this.serviceAddress = serviceAddress;
        }

        public int getPackageId() {
            return packageId;
        }

        public void setPackageId(int packageId) {
            this.packageId = packageId;
        }

        public LocalDateTime getAppointmentDateTime() {
            return appointmentDateTime;
        }

        public void setAppointmentDateTime(
                LocalDateTime appointmentDateTime) {

            this.appointmentDateTime = appointmentDateTime;
        }
    }
}
