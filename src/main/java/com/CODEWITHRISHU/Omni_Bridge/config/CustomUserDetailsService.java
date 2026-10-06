package com.CODEWITHRISHU.Omni_Bridge.config;

import com.CODEWITHRISHU.Omni_Bridge.model.staff.StaffUser;
import com.CODEWITHRISHU.Omni_Bridge.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final StaffUserRepository repository;

    @Override
    public StaffUser loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<StaffUser> userInfo = repository.findByEmail(username);
        return userInfo.map(StaffUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + username));
    }

}