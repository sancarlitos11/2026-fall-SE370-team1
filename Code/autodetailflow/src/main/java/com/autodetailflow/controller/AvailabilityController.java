
package com.autodetailflow.controller;

import com.autodetailflow.service.AvailabilityService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public List<LocalDateTime> getAvailableTimes(
            @RequestParam LocalDate date,
            @RequestParam(defaultValue = "120") int duration) {

        LocalDateTime openingTime = date.atTime(9, 0);
        LocalDateTime closingTime = date.atTime(17, 0);

        // Temporary sample data until database integration.
        List<LocalDateTime> existingStarts = new ArrayList<>();
        List<Integer> existingDurations = new ArrayList<>();

        existingStarts.add(date.atTime(11, 0));
        existingDurations.add(120);

        return availabilityService.getAvailableTimes(
                openingTime,
                closingTime,
                duration,
                existingStarts,
                existingDurations
        );
    }
}
