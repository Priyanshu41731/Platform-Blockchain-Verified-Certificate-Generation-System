package com.certificate.platform.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String organizationName;
    private String designation;
}