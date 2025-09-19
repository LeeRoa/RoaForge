package com.roa.forge.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:change-me-very-long-32bytes-minimum}")
    private String secret;

    @Value("${jwt.access-expiration-ms:3600000}")
    private long accessExpMs;

    @Value("${jwt.refresh-expiration-ms:1209600000}")
    private long refreshExpMs;
    private Key key;

    @PostConstruct
    public void init() {
        // HS256은 최소 256bit(=32바이트) 이상 키 권장
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String email, String username, String provider) {
        return Jwts.builder()
                .setId(java.util.UUID.randomUUID().toString())   // jti
                .setSubject(String.valueOf(userId))
                .claim("email", email)     // 선택: 편의 클레임
                .claim("uname", username)
                .claim("prov", provider)   // LOCAL/GOOGLE/...
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .setId(java.util.UUID.randomUUID().toString())
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getJti(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getId(); // 과거 발급분은 null 가능
    }

    public Date getExpiration(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getExpiration();
    }

    /** 유효성 검증 */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** raw subject 꺼내기 (userId 문자열) */
    public String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /** 숫자 userId로 파싱 */
    public Long getUserId(String token) {
        return Long.parseLong(getSubject(token));
    }

    /* ===== (선택) 구버전 호환을 잠깐 위해 남겨둘 수 있는 오버로드 ===== */
    @Deprecated
    public String createAccessToken(String legacySubject) {
        return Jwts.builder()
                .setSubject(legacySubject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Deprecated
    public String createRefreshToken(String legacySubject) {
        return Jwts.builder()
                .setSubject(legacySubject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}