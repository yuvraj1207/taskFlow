package com.taskflow.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Runs on every request that goes through the gateway, before routing.
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    // Only login and registration are open. Everything else needs a token.
    private static final List<String> PUBLIC_PREFIXES = List.of("/api/auth/");

    private static final String HEADER_USER = "X-User-Name";
    private static final String HEADER_ROLE = "X-User-Role";

    private final SecretKey key;

    public JwtAuthFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Step 1: remove any identity headers the client sent. Only the gateway may set them.
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove(HEADER_USER);
                    h.remove(HEADER_ROLE);
                });

        String path = exchange.getRequest().getPath().value();
        if (PUBLIC_PREFIXES.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        // Step 2: require a valid Bearer token
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return reject(exchange);
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(header.substring(7))
                    .getPayload();

            // Step 3: pass the verified identity downstream
            builder.headers(h -> {
                h.set(HEADER_USER, claims.getSubject());
                h.set(HEADER_ROLE, claims.get("role", String.class));
            });
            return chain.filter(exchange.mutate().request(builder.build()).build());
        } catch (JwtException | IllegalArgumentException ex) {
            // Bad signature, expired, or malformed token
            return reject(exchange);
        }
    }

    private Mono<Void> reject(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    // Lower numbers run first. -1 runs before the routing filters.
    @Override
    public int getOrder() {
        return -1;
    }
}