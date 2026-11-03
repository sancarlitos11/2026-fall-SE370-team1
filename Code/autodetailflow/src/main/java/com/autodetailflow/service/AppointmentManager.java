package com.autodetailflow.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.autodetailflow.dao.AppointmentDAOInterface;
import com.autodetailflow.model.Appointment;

@Service
public class AppointmentManager {

    private final AppointmentDAOInterface appointmentDAO;
    private final AvailabilityService availabilityService;

    public AppointmentManager(AppointmentDAOInterface appointmentDAO, AvailabilityService availabilityService) {
        this.appointmentDAO = appointmentDAO;
        this.availabilityService = availabilityService;
    }

    public boolean saveAppointment(Appointment appointment) {
        if (availabilityService.blocksAvailability(appointment.getStatus()) && !isAppointmentTimeAvailable(appointment)) {
            return false;
        }
        appointmentDAO.saveAppointment(appointment);
        return true;
    }

    public Appointment getAppointment(int appointmentId) {
        return appointmentDAO.getAppointment(appointmentId);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDAO.getAllAppointments();
    }
    public boolean isAppointmentTimeAvailable(Appointment requestedAppointment) {

        for (Appointment existing : appointmentDAO.getAllAppointments()) {
            if (existing.getAppointmentId() == requestedAppointment.getAppointmentId()) {
                continue;
            }

             if (!availabilityService.blocksAvailability(existing.getStatus())) {
                continue;
            }

            boolean available = availabilityService.isTimeAvailable(
                requestedAppointment.getAppointmentDateTime(),
                requestedAppointment.getDetailingPackage().getPackageDuration(),
                existing.getAppointmentDateTime(),
                existing.getDetailingPackage().getPackageDuration()
            );
            if (!available) {
                return false;
            }
        }
        return true;
    }

    // Approve pending appointment, change the status to "Scheduled". 
    public boolean approveAppointment(int appointmentId) {
        Appointment appointment = appointmentDAO.getAppointment(appointmentId);

        if (appointment == null) {
            return false;
        }

        if (!appointment.getStatus().equalsIgnoreCase("Pending")) {
            return false; 
        }

        appointment.setStatus("Scheduled");
        appointmentDAO.saveAppointment(appointment);
        return true;
    }
    // Decline pending appointment, change the status to "Declined".
    public boolean declineAppointment(int appointmentId) {
        Appointment appointment = appointmentDAO.getAppointment(appointmentId);

        if (appointment == null) {
            return false;
        }

        if (!appointment.getStatus().equalsIgnoreCase("Pending")) {
            return false; 
        }

        appointment.setStatus("Declined");
        appointmentDAO.saveAppointment(appointment);
        return true;
    }
    // Cancel pending or scheduled appointment, change the status to "Cancelled".
    public boolean cancelAppointment(int appointmentId) {
        Appointment appointment = appointmentDAO.getAppointment(appointmentId);

        if (appointment == null) {
            return false;
        }
        
        String status = appointment.getStatus();

        if (!status.equalsIgnoreCase("Pending") && !status.equalsIgnoreCase("Scheduled")) {
            return false; 
        }

        appointment.setStatus("Cancelled");
        appointmentDAO.saveAppointment(appointment);
        return true;
    }

    // Complete scheduled appointment, change the status to "Completed".
    public boolean completeAppointment(int appointmentId) {
        Appointment appointment = appointmentDAO.getAppointment(appointmentId);

        if (appointment == null) {
            return false;
        }

        if (!appointment.getStatus().equalsIgnoreCase("Scheduled")) {
            return false; 
        }

        appointment.setStatus("Completed");
        appointmentDAO.saveAppointment(appointment);
        return true;
    }

    public void deleteAppointment(int appointmentId) {
        appointmentDAO.deleteAppointment(appointmentId);
    }
}
