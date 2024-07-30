package com.ths.authentication_demo.dto;


public record SignupRequest (
    String name,
    String email,
    String password
) {
}
