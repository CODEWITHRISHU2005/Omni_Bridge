package com.CODEWITHRISHU.Omni_Bridge.controller.web;

import com.CODEWITHRISHU.Omni_Bridge.dto.IncidentApiModels.ReportRequest;
import com.CODEWITHRISHU.Omni_Bridge.model.incident.Incident;
import com.CODEWITHRISHU.Omni_Bridge.model.incident.IncidentType;
import com.CODEWITHRISHU.Omni_Bridge.model.incident.Severity;
import com.CODEWITHRISHU.Omni_Bridge.model.staff.Venue;
import com.CODEWITHRISHU.Omni_Bridge.service.IncidentService;
import com.CODEWITHRISHU.Omni_Bridge.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GuestReportController {
    private final VenueService venueService;
    private final IncidentService incidentService;

    public GuestReportController(
            VenueService venueService, IncidentService incidentService) {
        this.venueService = venueService;
        this.incidentService = incidentService;
    }

    @GetMapping("/report/{venueSlug}")
    public String reportForm(@PathVariable String venueSlug, Model model) {
        addFormData(venueSlug, model,
                new ReportRequest(null, null, "", "", null, null));
        return "report-form";
    }

    @PostMapping("/report/{venueSlug}")
    public String submitReport(
            @PathVariable String venueSlug,
            @Valid @ModelAttribute("reportRequest") ReportRequest request,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            addFormData(venueSlug, model, request);
            return "report-form";
        }

        Incident incident = incidentService.createReport(venueSlug, request);
        return "redirect:/report/confirmation/" + incident.getId();
    }

    @GetMapping("/report/confirmation/{incidentId}")
    public String confirmation(@PathVariable Long incidentId, Model model) {
        model.addAttribute("incidentId", incidentId);
        return "report-confirmation";
    }

    private void addFormData(
            String venueSlug, Model model, ReportRequest request) {
        Venue venue = venueService.getBySlug(venueSlug);
        model.addAttribute("venue", venue);
        model.addAttribute("reportRequest", request);
        model.addAttribute("incidentTypes", IncidentType.values());
        model.addAttribute("severities", Severity.values());
    }
}
