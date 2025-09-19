package com.roa.forge.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRefreshTokenDenylist implements RefreshTokenDenylist {
    private final Map<String, Long> revoked = new ConcurrentHashMap<>();

    @Override
    public void revoke(String jti, long expMs) {
        if (jti != null) revoked.put(jti, expMs);
    }

    @Override
    public boolean isRevoked(String jti) {
        if (jti == null) return false; // 옛 토큰(jti 없음)은 재사용 차단 불가
        Long exp = revoked.get(jti);
        if (exp == null) return false;
        if (exp < System.currentTimeMillis()) { revoked.remove(jti); return false; }
        return true;
    }
}