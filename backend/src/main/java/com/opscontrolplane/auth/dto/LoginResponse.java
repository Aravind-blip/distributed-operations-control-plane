package com.opscontrolplane.auth.dto;

public record LoginResponse(String token, String email, String role) {
}
