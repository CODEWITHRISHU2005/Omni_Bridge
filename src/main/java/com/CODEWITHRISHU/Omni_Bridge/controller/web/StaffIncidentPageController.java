package com.CODEWITHRISHU.Omni_Bridge.controller.web;

import com.CODEWITHRISHU.Omni_Bridge.dto.IncidentApiModels.IncidentUpdateRequest;
import com.CODEWITHRISHU.Omni_Bridge.model.incident.IncidentStatus;
import com.CODEWITHRISHU.Omni_Bridge.model.staff.StaffUser;
import com.CODEWITHRISHU.Omni_Bridge.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StaffIncidentPageController {
    private final IncidentService incidentService;

    public StaffIncidentPageController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/staff/incidents")
    public String board(
            @AuthenticationPrincipal StaffUser staff, Model model) {
        model.addAttribute("incidents", incidentService.listOpenIncidents(staff));
        model.addAttribute("staffName", staff.getName());
        return "staff/incidents";
    }

    @GetMapping("/staff/incidents/{incidentId}")
    public String detail(
            @PathVariable Long incidentId,
            @AuthenticationPrincipal StaffUser staff,
            Model model) {
        model.addAttribute("incident", incidentService.getForStaff(incidentId, staff));
        model.addAttribute("timeline", incidentService.getTimeline(incidentId, staff));
        model.addAttribute("statuses", IncidentStatus.values());
        model.addAttribute("updateRequest", new IncidentUpdateRequest(""));
        return "staff/incident-detail";
    }

    @PostMapping("/staff/incidents/{incidentId}/acknowledge")
    public String acknowledge(
            @PathVariable Long incidentId,
            @AuthenticationPrincipal StaffUser staff,
            RedirectAttributes redirect) {
        return perform(incidentId, staff, redirect,
                () -> incidentService.acknowledge(incidentId, staff));
    }

    @PostMapping("/staff/incidents/{incidentId}/assign-to-self")
    public String assignToSelf(
            @PathVariable Long incidentId,
            @AuthenticationPrincipal StaffUser staff,
            RedirectAttributes redirect) {
        return perform(incidentId, staff, redirect,
                () -> incidentService.assignToSelf(incidentId, staff));
    }

    @PostMapping("/staff/incidents/{incidentId}/status")
    public String changeStatus(
            @PathVariable Long incidentId,
            @RequestParam IncidentStatus status,
            @AuthenticationPrincipal StaffUser staff,
            RedirectAttributes redirect) {
        return perform(incidentId, staff, redirect,
                () -> incidentService.changeStatus(incidentId, status, staff));
    }

    @PostMapping("/staff/incidents/{incidentId}/updates")
    public String addUpdate(
            @PathVariable Long incidentId,
            @Valid @ModelAttribute("updateRequest") IncidentUpdateRequest request,
            BindingResult result,
            @AuthenticationPrincipal StaffUser staff,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", "Enter an update (max 1000 characters).");
            return redirectToDetail(incidentId);
        }
        return perform(incidentId, staff, redirect,
                () -> incidentService.addUpdate(incidentId, request, staff));
    }

    private String perform(
            Long incidentId, StaffUser staff,
            RedirectAttributes redirect, Runnable action) {
        try {
            action.run();
            redirect.addFlashAttribute("message", "Incident updated.");
        } catch (RuntimeException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return redirectToDetail(incidentId);
    }

    private String redirectToDetail(Long incidentId) {
        return "redirect:/staff/incidents/" + incidentId;
    }
}
