package com.ths.authentication_demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordResponse {
    private String status;
    private String message;
}
