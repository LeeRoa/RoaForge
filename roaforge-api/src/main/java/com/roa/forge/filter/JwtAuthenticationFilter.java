package com.roa.forge.filter;

import com.roa.forge.entity.UserAccount;
import com.roa.forge.provider.JwtTokenProvider;
import com.roa.forge.repository.UserAccountRepository;
import com.roa.forge.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;          // sub = userId 로 파싱
    private final UserAccountRepository userAccountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = header.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                try {
                    Long userId = jwtTokenProvider.getUserId(token);
                    UserAccount user = userAccountRepository.findById(userId).orElse(null);

                    if (user != null && Boolean.TRUE.equals(user.getActive())) {
                        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                                .map(r -> {
                                    String name = r.getName();
                                    return new SimpleGrantedAuthority(
                                            name.startsWith("ROLE_") ? name : "ROLE_" + name
                                    );
                                })
                                .collect(Collectors.toSet());

                        var authentication = new UsernamePasswordAuthenticationToken(
                                user.getUsername(),           // principal
                                null,                         // no credentials
                                authorities
                        );
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (NumberFormatException ignore) {
                    // sub가 숫자가 아니면(예: 구토큰), 그냥 통과 (필요 시 아래 호환 버전 사용)
                }
            }
        }

        chain.doFilter(request, response);
    }
}