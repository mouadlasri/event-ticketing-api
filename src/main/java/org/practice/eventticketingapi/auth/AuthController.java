package org.practice.eventticketingapi.auth;

import jakarta.validation.Valid;
import org.practice.eventticketingapi.auth.dto.LoginRequest;
import org.practice.eventticketingapi.auth.dto.LoginResponse;
import org.practice.eventticketingapi.user.UserService;
import org.practice.eventticketingapi.user.dto.CreateUserRequest;
import org.practice.eventticketingapi.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse token = authService.login(loginRequest);

        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid CreateUserRequest createUserRequest) {
        UserResponse userResponse = userService.createUser(createUserRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
