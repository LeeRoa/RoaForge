package com.roa.forge.provider;

import com.roa.forge.dto.ErrorCode;
import com.roa.forge.exception.AppException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration-ms:3600000}")
    private long accessExpMs;

    @Value("${jwt.refresh-expiration-ms:1209600000}")
    private long refreshExpMs;
    private Key key;


    @PostConstruct
    public void init() {
        String s = secret == null ? "" : secret.trim();

        // ★ 진단 로그 (운영에선 제거)
        System.out.println("[JWT] raw secret value: '" + s + "'");
        System.out.println("[JWT] startsWith base64: " + s.startsWith("base64:"));
        System.out.println("[JWT] startsWith hex   : " + s.startsWith("hex:"));


        byte[] keyBytes;
        if (s.startsWith("base64:")) {
            keyBytes = Base64.getDecoder().decode(s.substring(7));
        } else if (s.startsWith("hex:")) {
            String hex = s.substring(4);
            if (hex.startsWith("0x") || hex.startsWith("0X")) hex = hex.substring(2);
            keyBytes = Hex.decode(hex);
        } else {
            keyBytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new AppException(ErrorCode.JWT_SECRET_TOO_SHORT);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userId, String email, String username, String provider) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())   // jti
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("uname", username)
                .claim("prov", provider)   // LOCAL/GOOGLE/...
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
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

    /**
     * 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * raw subject 꺼내기 (userId 문자열)
     */
    public String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * 숫자 userId로 파싱
     */
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