package org.cybersaarthi.auth.service;

import org.cybersaarthi.auth.dto.AuthResponse;
import org.cybersaarthi.auth.dto.LoginRequest;
import org.cybersaarthi.auth.dto.RegisterRequest;
import org.cybersaarthi.auth.dto.UserSummaryResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserSummaryResponse getCurrentUser(UUID userId);
}
