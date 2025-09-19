package com.roa.forge.config;

public interface RefreshTokenDenylist {
    void revoke(String refreshTokenJti, long expiresAtEpochMs);
    boolean isRevoked(String refreshTokenJti);
}