package com.ths.authentication_demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRefreshResponse {
    private String token;
    private String refreshToken;
}
