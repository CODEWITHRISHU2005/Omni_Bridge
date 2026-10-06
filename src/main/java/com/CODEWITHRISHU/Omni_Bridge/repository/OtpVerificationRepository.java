package com.CODEWITHRISHU.Omni_Bridge.repository;

import com.CODEWITHRISHU.Omni_Bridge.model.OtpVerification;
import com.CODEWITHRISHU.Omni_Bridge.model.staff.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(String phone);

    Optional<OtpVerification> findTopByPhoneAndVerifiedTrueOrderByCreatedAtDesc(String phone);

    void deleteByPhoneAndVerifiedFalse(String phone);

    int deleteByExpiresAtBefore(Instant now);

    void deleteByUserAndVerifiedTrue(StaffUser user);
}