package com.rentguard.auth.service;

import com.rentguard.auth.dto.AuthRequest;
import com.rentguard.auth.dto.AuthResponse;
import com.rentguard.auth.dto.RegisterRequest;
import com.rentguard.auth.jwt.JwtTokenProvider;
import com.rentguard.common.exception.BadRequestException;
import com.rentguard.common.exception.ResourceNotFoundException;
import com.rentguard.user.model.Owner;
import com.rentguard.user.model.Role;
import com.rentguard.user.model.User;
import com.rentguard.user.model.UserStatus;
import com.rentguard.user.repository.OwnerRepository;
import com.rentguard.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       OwnerRepository ownerRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = new User(
                request.getName(),
                request.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                request.getRole()
        );

        user = userRepository.save(user);

        Long ownerId = null;
        if (user.getRole() == Role.ROLE_OWNER) {
            Owner owner = new Owner(user);
            owner = ownerRepository.save(owner);
            ownerId = owner.getId();
        }

        String token = tokenProvider.generateTokenFromUsername(user.getEmail());

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), ownerId);
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BadRequestException("Your account has been suspended by an administrator");
        }

        String token = tokenProvider.generateToken(authentication);

        Long ownerId = null;
        if (user.getRole() == Role.ROLE_OWNER) {
            ownerId = ownerRepository.findByUserId(user.getId())
                    .map(Owner::getId)
                    .orElse(null);
        }

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), ownerId);
    }
}
