package com.CODEWITHRISHU.Omni_Bridge.controller.api;

import com.CODEWITHRISHU.Omni_Bridge.dto.request.RefreshTokenRequest;
import com.CODEWITHRISHU.Omni_Bridge.dto.response.JwtResponse;
import com.CODEWITHRISHU.Omni_Bridge.model.RefreshToken;
import com.CODEWITHRISHU.Omni_Bridge.model.staff.StaffUser;
import com.CODEWITHRISHU.Omni_Bridge.service.AuthService;
import com.CODEWITHRISHU.Omni_Bridge.service.JwtService;
import com.CODEWITHRISHU.Omni_Bridge.service.OtpService;
import com.CODEWITHRISHU.Omni_Bridge.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signUp")
    public JwtResponse registerAndGetAccessAndRefreshToken(@RequestBody StaffUser userInfo) {
        authService.register(userInfo);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfo.getEmail());

        return JwtResponse.builder()
                .accessToken(jwtService.generateToken(userInfo))
                .refreshToken(refreshToken.getToken()).build();
    }

    @PostMapping("/refreshToken")
    public JwtResponse getRefreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return refreshTokenService.findByToken(refreshTokenRequest.token())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.generateToken(userInfo);
                    return JwtResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshTokenRequest.token())
                            .build();
                }).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Refresh token is invalid, expired, or not found in database.")
                );
    }

}