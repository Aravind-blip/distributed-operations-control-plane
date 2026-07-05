package com.opscontrolplane.auth;

import com.opscontrolplane.auth.dto.CurrentUserResponse;
import com.opscontrolplane.auth.dto.LoginRequest;
import com.opscontrolplane.auth.dto.LoginResponse;
import com.opscontrolplane.users.User;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        User user = authService.getByEmail(authentication.getName());
        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getRole().name());
    }
}
