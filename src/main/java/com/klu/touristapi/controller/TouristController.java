package com.klu.touristapi.controller;

import com.klu.touristapi.model.ApiError;
import com.klu.touristapi.model.Tourist;
import com.klu.touristapi.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/tourists")
@Tag(name = "Tourists", description = "Endpoints for managing tourists")
public class TouristController {

    private static final String VALID_TOKEN = "BORDER123";

    private final JwtUtil jwtUtil;

    private final List<Tourist> tourists = new CopyOnWriteArrayList<>(List.of(
            new Tourist(1, "Rahul", "India", "P12345"),
            new Tourist(2, "John", "USA", "P67890"),
            new Tourist(3, "David", "UK", "P54321")
    ));

    public TouristController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    @Operation(summary = "Get all tourists")
    public ResponseEntity<List<Tourist>> getAllTourists() {
        return ResponseEntity.ok(tourists);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tourist details by ID", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<?> getTourist(
            @PathVariable int id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String xAuthToken,
            HttpServletRequest request) {

        String token = resolveToken(authHeader, xAuthToken);

        if (token == null) {
            ApiError error = new ApiError(
                    500,
                    "Internal Server Error",
                    "Missing X-Auth-Token header",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        boolean isValid = VALID_TOKEN.equals(token) || jwtUtil.validateToken(token);
        if (!isValid) {
            ApiError error = new ApiError(
                    500,
                    "Internal Server Error",
                    "Invalid X-Auth-Token value",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        for (Tourist tourist : tourists) {
            if (tourist.getId() == id) {
                return ResponseEntity.ok(tourist);
            }
        }

        ApiError error = new ApiError(
                404,
                "Not Found",
                "Tourist with ID " + id + " was not found",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tourist details after validating JWT token", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<?> updateTourist(
            @PathVariable int id,
            @RequestBody Tourist updatedData,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String xAuthToken,
            HttpServletRequest request) {

        String token = resolveToken(authHeader, xAuthToken);

        if (token == null) {
            ApiError error = new ApiError(
                    500,
                    "Internal Server Error",
                    "Missing Authorization token",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        if (!jwtUtil.validateToken(token)) {
            ApiError error = new ApiError(
                    500,
                    "Internal Server Error",
                    "Invalid JWT token",
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        for (Tourist tourist : tourists) {
            if (tourist.getId() == id) {
                if (updatedData.getName() != null) {
                    tourist.setName(updatedData.getName());
                }
                if (updatedData.getCountry() != null) {
                    tourist.setCountry(updatedData.getCountry());
                }
                if (updatedData.getPassportNumber() != null) {
                    tourist.setPassportNumber(updatedData.getPassportNumber());
                }
                return ResponseEntity.ok(tourist);
            }
        }

        ApiError error = new ApiError(
                404,
                "Not Found",
                "Tourist with ID " + id + " was not found",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    private String resolveToken(String authHeader, String xAuthToken) {
        if (authHeader != null && !authHeader.isBlank()) {
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7).trim();
            }
            return authHeader.trim();
        }
        if (xAuthToken != null && !xAuthToken.isBlank()) {
            if (xAuthToken.startsWith("Bearer ")) {
                return xAuthToken.substring(7).trim();
            }
            return xAuthToken.trim();
        }
        return null;
    }
}
