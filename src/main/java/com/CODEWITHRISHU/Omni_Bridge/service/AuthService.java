package com.CODEWITHRISHU.Omni_Bridge.service;

import com.CODEWITHRISHU.Omni_Bridge.model.staff.StaffUser;
import com.CODEWITHRISHU.Omni_Bridge.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final StaffUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public void register(StaffUser userInfo) {
        log.info("Attempting to register user: {}", userInfo.getEmail());

        repository.findByEmail(userInfo.getEmail())
                .ifPresent(u -> {
                    throw new UserAlreadyExists("User already exists with email: " + userInfo.getEmail());
                });

        repository.save(prepareUser(userInfo));

        log.info("User '{}' added successfully", userInfo.getEmail());
    }

    private StaffUser prepareUser(StaffUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role assignedRole = Optional.ofNullable(user.getAdminKey())
                .filter(key -> key.equals("Rishabh@2005"))
                .map(key -> Role.ADMIN)
                .orElse(Role.USER);

        user.setRoles(Set.of(assignedRole));
        user.setAdminKey(null);

        log.info("User '{}' assigned role: {}", user.getEmail(), assignedRole);
        return user;
    }
}