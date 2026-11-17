package com.klu.touristapi.controller;

import com.klu.touristapi.model.AuthRequest;
import com.klu.touristapi.model.AuthResponse;
import com.klu.touristapi.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for generating JWT tokens")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @Operation(summary = "Generate JWT token using credentials")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        String username = authRequest != null && authRequest.getUsername() != null && !authRequest.getUsername().isBlank()
                ? authRequest.getUsername()
                : "touristUser";
        String token = jwtUtil.generateToken(username);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/token")
    @Operation(summary = "Generate a sample JWT token directly")
    public ResponseEntity<AuthResponse> getToken(@RequestParam(defaultValue = "touristUser") String username) {
        String token = jwtUtil.generateToken(username);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}

