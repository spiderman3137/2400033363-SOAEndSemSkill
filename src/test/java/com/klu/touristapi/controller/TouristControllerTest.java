package com.klu.touristapi.controller;

import com.klu.touristapi.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TouristController.class)
@Import(JwtUtil.class)
class TouristControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void validTokenReturnsTourist() throws Exception {
        mockMvc.perform(get("/tourists/1").header("X-Auth-Token", "BORDER123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rahul"));
    }

    @Test
    void invalidTokenReturnsErrorDetails() throws Exception {
        mockMvc.perform(get("/tourists/1").header("X-Auth-Token", "WRONG123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Invalid X-Auth-Token value"));
    }

    @Test
    void missingTokenReturnsErrorDetails() throws Exception {
        mockMvc.perform(get("/tourists/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Missing X-Auth-Token header"));
    }

    @Test
    void unknownTouristReturns404() throws Exception {
        mockMvc.perform(get("/tourists/99").header("X-Auth-Token", "BORDER123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tourist with ID 99 was not found"));
    }

    @Test
    void validJwtTokenUpdatesTourist() throws Exception {
        String token = jwtUtil.generateToken("testUser");
        String requestBody = "{\"name\":\"Updated Rahul\",\"country\":\"India\",\"passportNumber\":\"P99999\"}";

        mockMvc.perform(put("/tourists/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Rahul"))
                .andExpect(jsonPath("$.passportNumber").value("P99999"));
    }

    @Test
    void invalidJwtTokenReturns500OnPut() throws Exception {
        String requestBody = "{\"name\":\"Updated Rahul\",\"country\":\"India\",\"passportNumber\":\"P99999\"}";

        mockMvc.perform(put("/tourists/1")
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Invalid JWT token"));
    }

    @Test
    void missingTokenReturns500OnPut() throws Exception {
        String requestBody = "{\"name\":\"Updated Rahul\",\"country\":\"India\",\"passportNumber\":\"P99999\"}";

        mockMvc.perform(put("/tourists/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Missing Authorization token"));
    }

    @Test
    void unknownTouristReturns404OnPut() throws Exception {
        String token = jwtUtil.generateToken("testUser");
        String requestBody = "{\"name\":\"Unknown\",\"country\":\"Nowhere\",\"passportNumber\":\"00000\"}";

        mockMvc.perform(put("/tourists/99")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tourist with ID 99 was not found"));
    }
}
