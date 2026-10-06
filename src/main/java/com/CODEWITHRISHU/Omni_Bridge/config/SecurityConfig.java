package com.CODEWITHRISHU.Omni_Bridge.config;

import com.CODEWITHRISHU.Omni_Bridge.handler.JwtAuthFilter;
import com.CODEWITHRISHU.Omni_Bridge.handler.MagicLinkOttGenerationSuccessHandler;
import com.CODEWITHRISHU.Omni_Bridge.handler.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final RateLimitFilter rateLimitFilter;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        AuthorizationManager<RequestAuthorizationContext> mfa =
                AuthorizationManagers.allOf(
                        AuthorityAuthorizationManager.hasAuthority("OTP_AUTHORITY"),
                        AuthorityAuthorizationManager.hasAuthority(FactorGrantedAuthority.OTT_AUTHORITY)
                );
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/ott/**",
                                "/api/otp/**",
                                "/actuator/health/**",
                                "/favicon.ico",
                                "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        .anyRequest().access(mfa)
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .logout(AbstractHttpConfigurer::disable)
                .oneTimeTokenLogin(ott -> ott
                        .tokenGenerationSuccessHandler(oneTimeTokenGenerationSuccessHandler())
                        .permitAll())
                .exceptionHandling(eh -> eh.authenticationEntryPoint((req, resp, e) -> {
                    e.printStackTrace();
                    resp.setStatus(401);
                    resp.setContentType("application/json");

                    String message = (String) req.getAttribute("exception");

                    ObjectMapper om = new ObjectMapper();

                    if (message != null && message.trim().equals("token_expired")) {
                        resp.getWriter().println(om.writeValueAsString(Map.of("message", "token_expired")));
                    } else if (message != null && message.trim().equals("invalid_token")) {
                        resp.getWriter().println(om.writeValueAsString(Map.of("message", "invalid_token")));
                    } else {
                        resp.getWriter().println(om.writeValueAsString(Map.of("message", e.getMessage())));
                    }
                }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, JwtAuthFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public MagicLinkOttGenerationSuccessHandler oneTimeTokenGenerationSuccessHandler() {
        return new MagicLinkOttGenerationSuccessHandler();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}