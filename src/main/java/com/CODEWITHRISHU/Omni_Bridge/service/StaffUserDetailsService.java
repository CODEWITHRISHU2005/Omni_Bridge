package com.CODEWITHRISHU.Omni_Bridge.service;

import com.CODEWITHRISHU.Omni_Bridge.repository.StaffUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StaffUserDetailsService implements UserDetailsService {
    private final StaffUserRepository staffUserRepository;

    public StaffUserDetailsService(StaffUserRepository staffUserRepository) {
        this.staffUserRepository = staffUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return staffUserRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Staff account not found"));
    }
}
