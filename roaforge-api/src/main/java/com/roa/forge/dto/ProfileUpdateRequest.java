package com.roa.forge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(min = 2, max = 50) String username,
        @Email String email
) {}