package com.ths.authentication_demo.dto;

public record ApiErrorResponse(
    int errorCode,
    String description) {

}
