package com.wildroutes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}

@Data
class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}

@Data
class AuthResponse {
    private String token;
    private Long userId;
    private String username;
}

