package com.feedback.feedback360.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtCookieFilter extends OncePerRequestFilter {
    public static final String COOKIE_NAME = "fb360_token";
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                     FilterChain chain) throws ServletException, IOException {
        String token = null;
        if (req.getCookies() != null)
            for (Cookie c : req.getCookies()) if (COOKIE_NAME.equals(c.getName())) { token = c.getValue(); break; }

        if (token != null && jwtUtil.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = jwtUtil.parseClaims(token);
            var auth = new UsernamePasswordAuthenticationToken(
                    claims.get("userId", Long.class), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class))));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(req, res);
    }
}
