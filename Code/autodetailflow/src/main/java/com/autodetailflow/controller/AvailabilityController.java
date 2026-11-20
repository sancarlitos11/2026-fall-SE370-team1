
package com.autodetailflow.controller;

import com.autodetailflow.model.Appointment;
import com.autodetailflow.service.AppointmentService;
import com.autodetailflow.service.AvailabilityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;

    public AvailabilityController(
            AvailabilityService availabilityService,
            AppointmentService appointmentService) {

        this.availabilityService = availabilityService;
        this.appointmentService = appointmentService;
    }

    // Existing endpoint: customers view available appointment times.
    @GetMapping
    public List<LocalDateTime> getAvailableTimes(
            @RequestParam LocalDate date,
            @RequestParam(defaultValue = "120") int duration) {

        LocalDateTime openingTime = date.atTime(9, 0);
        LocalDateTime closingTime = date.atTime(17, 0);

        List<Appointment> appointments =
                appointmentService.getAllAppointments();

        return availabilityService.getAvailableTimesForAppointments(
                openingTime,
                closingTime,
                duration,
                appointments
        );
    }

    // Employee endpoint: block a time period.
    @PostMapping("/blocked")
    public ResponseEntity<?> blockTime(
            @RequestBody BlockTimeRequest request) {

        if (request == null
                || request.getStart() == null
                || request.getEnd() == null) {

            return ResponseEntity.badRequest().body(
                    Map.of("error", "Start and end times are required")
            );
        }

        LocalDateTime start = request.getStart();
        LocalDateTime end = request.getEnd();

        if (!start.isBefore(end)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "End time must be after start time")
            );
        }

        if (!start.toLocalDate().equals(end.toLocalDate())
                || start.getHour() < 9
                || end.isAfter(start.toLocalDate().atTime(17, 0))) {

            return ResponseEntity.badRequest().body(
                    Map.of("error",
                            "Blocked times must be within business hours, 9 AM to 5 PM")
            );
        }

        availabilityService.blockTime(start, end);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Time blocked successfully",
                        "start", start.toString(),
                        "end", end.toString()
                )
        );
    }

    // Employee endpoint: remove a blocked time period.
    @DeleteMapping("/blocked")
    public ResponseEntity<?> unblockTime(
            @RequestBody BlockTimeRequest request) {

        if (request == null
                || request.getStart() == null
                || request.getEnd() == null) {

            return ResponseEntity.badRequest().body(
                    Map.of("error", "Start and end times are required")
            );
        }

        boolean removed = availabilityService.unblockTime(
                request.getStart(),
                request.getEnd()
        );

        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", "Blocked time period not found")
            );
        }

        return ResponseEntity.ok(
                Map.of("message", "Time unblocked successfully")
        );
    }

    // Employee endpoint: see which standard appointment times are blocked.
    @GetMapping("/blocked")
    public List<LocalDateTime> getBlockedTimes(
            @RequestParam LocalDate date,
            @RequestParam(defaultValue = "120") int duration) {

        LocalDateTime openingTime = date.atTime(9, 0);
        LocalDateTime closingTime = date.atTime(17, 0);

        List<LocalDateTime> blockedSlots = new ArrayList<>();

        if (duration <= 0) {
            throw new IllegalArgumentException(
                    "Duration must be greater than zero"
            );
        }

        LocalDateTime currentTime = openingTime;

        while (!currentTime.plusMinutes(duration)
                .isAfter(closingTime)) {

            if (availabilityService.isBlocked(currentTime, duration)) {
                blockedSlots.add(currentTime);
            }

            currentTime = currentTime.plusMinutes(duration);
        }

        return blockedSlots;
    }

    // Request data for blocking or unblocking a time period.
    public static class BlockTimeRequest {

        private LocalDateTime start;
        private LocalDateTime end;

        public BlockTimeRequest() {
        }

        public LocalDateTime getStart() {
            return start;
        }

        public void setStart(LocalDateTime start) {
            this.start = start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public void setEnd(LocalDateTime end) {
            this.end = end;
        }
    }
}
