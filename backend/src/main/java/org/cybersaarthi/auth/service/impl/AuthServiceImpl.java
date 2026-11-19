package org.cybersaarthi.auth.service.impl;

import org.cybersaarthi.auth.dto.AuthResponse;
import org.cybersaarthi.auth.dto.LoginRequest;
import org.cybersaarthi.auth.dto.RegisterRequest;
import org.cybersaarthi.auth.dto.UserSummaryResponse;
import org.cybersaarthi.auth.service.AuthService;
import org.cybersaarthi.common.exception.DuplicateResourceException;
import org.cybersaarthi.common.exception.ResourceNotFoundException;
import org.cybersaarthi.security.jwt.JwtTokenProvider;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.cybersaarthi.user.entity.Role;
import org.cybersaarthi.user.entity.User;
import org.cybersaarthi.user.repository.RoleRepository;
import org.cybersaarthi.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("Email is already registered: " + normalizedEmail);
        }

        Role userRole = roleRepository.findByCode(DEFAULT_ROLE)
            .orElseGet(() -> roleRepository.save(new Role(DEFAULT_ROLE, "Standard registered user")));

        User user = new User(
            normalizedEmail,
            passwordEncoder.encode(request.password()),
            request.displayName().trim()
        );
        user.addRole(userRole);

        User savedUser = userRepository.save(user);
        log.info("Registered new user with ID: {}", savedUser.getId());

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtTokenProvider.generateToken(userDetails);

        return AuthResponse.of(
            token,
            jwtTokenProvider.getExpirationMs(),
            UserSummaryResponse.fromEntity(savedUser)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", normalizedEmail));

        log.info("User logged in successfully: {}", user.getId());

        return AuthResponse.of(
            token,
            jwtTokenProvider.getExpirationMs(),
            UserSummaryResponse.fromEntity(user)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserSummaryResponse.fromEntity(user);
    }
}
