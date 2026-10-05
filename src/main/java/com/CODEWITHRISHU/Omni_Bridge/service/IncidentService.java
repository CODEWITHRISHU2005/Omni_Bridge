package com.CODEWITHRISHU.Omni_Bridge.service;

import com.CODEWITHRISHU.Omni_Bridge.dto.IncidentApiModels.IncidentUpdateRequest;
import com.CODEWITHRISHU.Omni_Bridge.dto.IncidentApiModels.ReportRequest;
import com.CODEWITHRISHU.Omni_Bridge.exception.IncidentNotFoundException;
import com.CODEWITHRISHU.Omni_Bridge.exception.InvalidIncidentTransitionException;
import com.CODEWITHRISHU.Omni_Bridge.exception.VenueNotFoundException;
import com.CODEWITHRISHU.Omni_Bridge.model.*;
import com.CODEWITHRISHU.Omni_Bridge.repository.IncidentRepository;
import com.CODEWITHRISHU.Omni_Bridge.repository.IncidentUpdateRepository;
import com.CODEWITHRISHU.Omni_Bridge.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {
    private static final List<IncidentStatus> HIDDEN_FROM_OPEN_BOARD =
            List.of(IncidentStatus.CLOSED, IncidentStatus.DUPLICATE);

    private final IncidentRepository incidentRepository;
    private final IncidentUpdateRepository updateRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public Incident createReport(String venueSlug, ReportRequest request) {
        Venue venue = venueRepository.findBySlug(venueSlug)
                .orElseThrow(() -> new VenueNotFoundException(venueSlug));

        Incident incident = new Incident(
                venue,
                request.type(),
                request.severity(),
                request.description().trim(),
                request.location().trim(),
                clean(request.reporterName()),
                clean(request.reporterPhone()));

        incident = incidentRepository.save(incident);
        updateRepository.save(new IncidentUpdate(
                incident, null, "Report submitted by guest."));

        return incident;
    }

    @Transactional(readOnly = true)
    public List<Incident> listOpenIncidents(StaffUser staff) {
        return incidentRepository.findOpenForVenue(
                staff.getVenue().getId(), HIDDEN_FROM_OPEN_BOARD);
    }

    @Transactional(readOnly = true)
    public Incident getForStaff(Long incidentId, StaffUser staff) {
        return incidentRepository
                .findByIdAndVenueId(incidentId, staff.getVenue().getId())
                .orElseThrow(() -> new IncidentNotFoundException(incidentId));
    }

    @Transactional(readOnly = true)
    public List<IncidentUpdate> getTimeline(Long incidentId, StaffUser staff) {
        Incident incident = getForStaff(incidentId, staff);
        return updateRepository.findByIncidentIdOrderByCreatedAtAsc(incident.getId());
    }

    @Transactional
    public void acknowledge(Long incidentId, StaffUser staff) {
        changeStatus(incidentId, IncidentStatus.ACKNOWLEDGED, staff);
    }

    @Transactional
    public void assignToSelf(Long incidentId, StaffUser staff) {
        Incident incident = getForStaff(incidentId, staff);
        incident.setAssignedTo(staff);
        incidentRepository.save(incident);
        updateRepository.save(new IncidentUpdate(
                incident, staff, "Assigned to " + staff.getName() + "."));
    }

    @Transactional
    public void addUpdate(
            Long incidentId, IncidentUpdateRequest request, StaffUser staff) {
        Incident incident = getForStaff(incidentId, staff);
        updateRepository.save(new IncidentUpdate(
                incident, staff, request.message().trim()));
    }

    @Transactional
    public void changeStatus(
            Long incidentId, IncidentStatus next, StaffUser staff) {
        Incident incident = getForStaff(incidentId, staff);
        IncidentStatus previous = incident.getStatus();

        validateTransition(previous, next);

        incident.setStatus(next);
        incidentRepository.save(incident);
        updateRepository.save(new IncidentUpdate(
                incident, staff,
                "Status changed from " + previous + " to " + next + "."));
    }

    private void validateTransition(
            IncidentStatus current, IncidentStatus next) {
        boolean allowed = switch (current) {
            case REPORTED -> next == IncidentStatus.ACKNOWLEDGED
                    || next == IncidentStatus.DUPLICATE;
            case ACKNOWLEDGED -> next == IncidentStatus.RESPONDING
                    || next == IncidentStatus.DUPLICATE;
            case RESPONDING -> next == IncidentStatus.RESOLVED
                    || next == IncidentStatus.DUPLICATE;
            case RESOLVED -> next == IncidentStatus.CLOSED
                    || next == IncidentStatus.RESPONDING;
            case CLOSED, DUPLICATE -> false;
        };

        if (!allowed) {
            throw new InvalidIncidentTransitionException(current, next);
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
