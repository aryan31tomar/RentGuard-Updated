package com.rentguard.auth;

import com.rentguard.auth.dto.AuthRequest;
import com.rentguard.auth.dto.AuthResponse;
import com.rentguard.auth.dto.RegisterRequest;
import com.rentguard.auth.jwt.JwtTokenProvider;
import com.rentguard.auth.service.AuthService;
import com.rentguard.common.exception.BadRequestException;
import com.rentguard.user.model.Owner;
import com.rentguard.user.model.Role;
import com.rentguard.user.model.User;
import com.rentguard.user.repository.OwnerRepository;
import com.rentguard.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OwnerRepository ownerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should register new tenant and return JWT token")
    void testRegisterTenant_Success() {
        RegisterRequest request = new RegisterRequest("Test Tenant", "test@rentguard.com", "Password123!", "+91 99999 88888", Role.ROLE_TENANT);

        when(userRepository.existsByEmail("test@rentguard.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(tokenProvider.generateTokenFromUsername("test@rentguard.com")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("test@rentguard.com", response.getEmail());
        assertEquals(Role.ROLE_TENANT, response.getRole());
        verify(ownerRepository, never()).save(any(Owner.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException if email already registered")
    void testRegister_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Test", "existing@rentguard.com", "Password123!", null, Role.ROLE_TENANT);
        when(userRepository.existsByEmail("existing@rentguard.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Should login and return JWT token for valid credentials")
    void testLogin_Success() {
        AuthRequest request = new AuthRequest("user@rentguard.com", "Password123!");

        User user = new User("User", "user@rentguard.com", "hashedPass", null, Role.ROLE_TENANT);
        user.setId(5L);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("user@rentguard.com", "Password123!"));
        when(userRepository.findByEmail("user@rentguard.com")).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(any())).thenReturn("valid-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("valid-jwt-token", response.getToken());
        assertEquals("user@rentguard.com", response.getEmail());
    }
}
