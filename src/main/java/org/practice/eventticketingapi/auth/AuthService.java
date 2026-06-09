package org.practice.eventticketingapi.auth;

import org.practice.eventticketingapi.auth.dto.LoginRequest;
import org.practice.eventticketingapi.auth.dto.LoginResponse;
import org.practice.eventticketingapi.auth.exception.InvalidCredentialsException;
import org.practice.eventticketingapi.security.JwtService;
import org.practice.eventticketingapi.user.User;
import org.practice.eventticketingapi.user.UserRepository;
import org.practice.eventticketingapi.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(JwtService jwtService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmailAndDeletedAtIsNull(email).orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getId());

        return new LoginResponse(token);
    }
}
