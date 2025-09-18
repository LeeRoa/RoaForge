package com.roa.forge.dto;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record MeResponse(
        Long id,
        String username,
        String email,
        String provider,
        Collection<? extends GrantedAuthority> authorities
) {}