package com.roa.forge.service;

import com.roa.forge.dto.LoginRequest;
import com.roa.forge.dto.RegisterRequest;
import com.roa.forge.dto.TokenResponse;
import com.roa.forge.entity.UserAccount;

public interface AuthService {
    TokenResponse refresh(String refreshToken);
    TokenResponse login(LoginRequest request);
    void logout(String refreshToken);
    TokenResponse registerLocal(RegisterRequest req);
    UserAccount upsertGoogleUser(String sub, String email, String nameOrNull);
    TokenResponse issueTokensFor(UserAccount user);
}
