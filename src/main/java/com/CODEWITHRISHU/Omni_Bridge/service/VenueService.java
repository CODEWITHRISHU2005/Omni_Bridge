package com.CODEWITHRISHU.Omni_Bridge.service;

import com.CODEWITHRISHU.Omni_Bridge.exception.VenueNotFoundException;
import com.CODEWITHRISHU.Omni_Bridge.model.staff.Venue;
import com.CODEWITHRISHU.Omni_Bridge.repository.VenueRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VenueService {
    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue getBySlug(String slug) {
        return venueRepository.findBySlug(slug)
                .orElseThrow(() -> new VenueNotFoundException(slug));
    }

    public List<Venue> getAll() {
        return venueRepository.findAll();
    }
}
