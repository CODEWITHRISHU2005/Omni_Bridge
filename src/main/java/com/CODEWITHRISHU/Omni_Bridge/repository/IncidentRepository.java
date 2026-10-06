package com.CODEWITHRISHU.Omni_Bridge.repository;

import com.CODEWITHRISHU.Omni_Bridge.model.incident.Incident;
import com.CODEWITHRISHU.Omni_Bridge.model.incident.IncidentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    @Query("""
            select i from Incident i
            where i.venue.id = :venueId
              and i.status not in :excluded
            order by i.createdAt desc
            """)
    List<Incident> findOpenForVenue(
            @Param("venueId") Long venueId,
            @Param("excluded") List<IncidentStatus> excluded);

    Optional<Incident> findByIdAndVenueId(Long id, Long venueId);
}
