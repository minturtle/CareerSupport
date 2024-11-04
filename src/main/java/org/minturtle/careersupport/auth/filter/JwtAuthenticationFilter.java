package org.minturtle.careersupport.auth.filter;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.auth.utils.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenProvider jwtUtil;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        token = token.substring(7);
        final String finalToken = token;

        return Mono.justOrEmpty(finalToken)
                .flatMap(jwtUtil::verify)
                .map(user -> new UsernamePasswordAuthenticationToken(user, null, null))
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                .onErrorResume(JwtException.class, e -> chain.filter(exchange));
    }
}