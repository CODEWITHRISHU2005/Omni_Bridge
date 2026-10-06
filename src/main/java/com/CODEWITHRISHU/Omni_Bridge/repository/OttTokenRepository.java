package com.CODEWITHRISHU.Omni_Bridge.repository;

import com.CODEWITHRISHU.Omni_Bridge.model.OttToken;
import com.CODEWITHRISHU.Omni_Bridge.model.staff.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface OttTokenRepository extends JpaRepository<OttToken, Integer> {
    Optional<OttToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM OttToken o WHERE o.token = :token")
    void deleteByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM OttToken o WHERE o.user = :user")
    void deleteByUser(StaffUser user);
}