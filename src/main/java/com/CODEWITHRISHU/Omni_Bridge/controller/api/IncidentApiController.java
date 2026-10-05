package com.CODEWITHRISHU.Omni_Bridge.controller.api;

import com.CODEWITHRISHU.Omni_Bridge.dto.IncidentApiModels.*;
import com.CODEWITHRISHU.Omni_Bridge.model.Incident;
import com.CODEWITHRISHU.Omni_Bridge.model.StaffUser;
import com.CODEWITHRISHU.Omni_Bridge.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IncidentApiController {
    private final IncidentService incidentService;

    public IncidentApiController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping("/report/{venueSlug}")
    public ResponseEntity<IncidentCreatedResponse> submitReport(
            @PathVariable String venueSlug,
            @Valid @RequestBody ReportRequest request) {
        Incident incident = incidentService.createReport(venueSlug, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new IncidentCreatedResponse(
                        incident.getId(),
                        incident.getStatus()));
    }

    @GetMapping("/staff/incidents")
    public List<IncidentResponse> incidentBoard(
            @AuthenticationPrincipal StaffUser staff) {
        return incidentService.listOpenIncidents(staff).stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @GetMapping("/staff/incidents/{incidentId}")
    public IncidentDetailResponse incidentDetail(
            @PathVariable Long incidentId,
            @AuthenticationPrincipal StaffUser staff) {
        Incident incident = incidentService.getForStaff(incidentId, staff);

        List<IncidentUpdateResponse> timeline = incidentService
                .getTimeline(incidentId, staff).stream()
                .map(IncidentUpdateResponse::from)
                .toList();

        return new IncidentDetailResponse(
                IncidentResponse.from(incident),
                timeline);
    }

    @PostMapping("/staff/incidents/{incidentId}/acknowledge")
    public ResponseEntity<Void> acknowledge(
            @PathVariable Long incidentId,
            @AuthenticationPrincipal StaffUser staff) {
        incidentService.acknowledge(incidentId, staff);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/staff/incidents/{incidentId}/assign-to-self")
    public ResponseEntity<Void> assignToSelf(
            @PathVariable Long incidentId,
            @AuthenticationPrincipal StaffUser staff) {
        incidentService.assignToSelf(incidentId, staff);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/staff/incidents/{incidentId}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long incidentId,
            @Valid @RequestBody StatusChangeRequest request,
            @AuthenticationPrincipal StaffUser staff) {
        incidentService.changeStatus(
                incidentId,
                request.status(),
                staff);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/staff/incidents/{incidentId}/updates")
    public ResponseEntity<Void> addUpdate(
            @PathVariable Long incidentId,
            @Valid @RequestBody IncidentUpdateRequest request,
            @AuthenticationPrincipal StaffUser staff) {
        incidentService.addUpdate(incidentId, request, staff);
        return ResponseEntity.noContent().build();
    }
}
