package com.removebg.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClerkJWTAuthFilter extends OncePerRequestFilter {

    private final ClerkProperties clerkProperties;
    private final ClerkJwksProvider jwksProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod())
                || "/api/payments/webhook".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            rejectUnauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        try {
            String token = authHeader.substring(7);
            String kid = extractKeyId(token);
            PublicKey publicKey = jwksProvider.getPublicKey(kid);

            Claims claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .setAllowedClockSkewSeconds(clerkProperties.getAllowedClockSkewSeconds())
                    .requireIssuer(clerkProperties.getIssuer())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            log.warn("Rejected request to {} because Clerk JWT validation failed", request.getRequestURI());
            rejectUnauthorized(response, "Invalid token");
        }
    }

    private String extractKeyId(String token) throws IOException {
        String[] chunks = token.split("\\.");
        if (chunks.length < 2) {
            throw new IllegalArgumentException("JWT is malformed");
        }

        String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]), StandardCharsets.UTF_8);
        JsonNode jsonNode = objectMapper.readTree(headerJson);
        JsonNode kidNode = jsonNode.get("kid");
        if (kidNode == null || !kidNode.isTextual()) {
            throw new IllegalArgumentException("JWT header does not contain a valid kid");
        }

        return kidNode.asText();
    }

    private void rejectUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
