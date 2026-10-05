package com.CODEWITHRISHU.Omni_Bridge.controller.api;

import com.CODEWITHRISHU.Omni_Bridge.dto.IncidentApiModels.VenueResponse;
import com.CODEWITHRISHU.Omni_Bridge.service.VenueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueApiController {
    private final VenueService venueService;

    public VenueApiController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public List<VenueResponse> listVenues() {
        return venueService.getAll().stream()
                .map(VenueResponse::from)
                .toList();
    }

    @GetMapping("/{slug}")
    public VenueResponse getVenue(@PathVariable String slug) {
        return VenueResponse.from(venueService.getBySlug(slug));
    }
}
