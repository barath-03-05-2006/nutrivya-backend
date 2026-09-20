package com.nutritrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Runs whenever a request reaches a protected endpoint without a valid
 * authentication. Replaces Spring Security's default bare 403 with a JSON
 * body the client can branch on: tokenExpired=true means "call
 * /api/auth/refresh and retry silently"; false means "no/garbage token,
 * send the user to login".
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
            throws IOException {
        boolean expired = Boolean.TRUE.equals(req.getAttribute(JwtFilter.EXPIRED_ATTR));
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(mapper.writeValueAsString(Map.of(
                "error", expired ? "Access token expired" : "Authentication required",
                "tokenExpired", expired
        )));
    }
}
