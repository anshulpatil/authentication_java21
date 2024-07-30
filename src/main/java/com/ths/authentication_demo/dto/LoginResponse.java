package com.ths.authentication_demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

@Builder
public record LoginResponse(
    String email,
    String token,
    String refreshToken,
    String status) {
}
