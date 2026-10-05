package com.CODEWITHRISHU.Omni_Bridge.repository;

import com.CODEWITHRISHU.Omni_Bridge.model.IncidentUpdate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentUpdateRepository
        extends JpaRepository<IncidentUpdate, Long> {
    List<IncidentUpdate> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);
}
