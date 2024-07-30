package com.ths.authentication_demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordDto {
    private String email;
    private String newPassword;
    private String confirmNewPassword;
}
