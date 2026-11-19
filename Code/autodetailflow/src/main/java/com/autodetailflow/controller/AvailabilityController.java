
package com.autodetailflow.controller;

import com.autodetailflow.model.Appointment;
import com.autodetailflow.service.AppointmentService;
import com.autodetailflow.service.AvailabilityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
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
}
