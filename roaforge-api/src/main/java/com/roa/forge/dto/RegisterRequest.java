package com.roa.forge.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank @Email
    @Size(max = 100)
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    private String password;
}