package org.minturtle.careersupport.auth.filter;

import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.auth.utils.ApiTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class ApiTokenFilter implements WebFilter {

    private final ApiTokenProvider apiTokenProvider;
    private final static String API_TOKEN_HEADER = "X-API-TOKEN";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(API_TOKEN_HEADER);

        if (token == null || !token.startsWith("cs_")) {
            return chain.filter(exchange);
        }

        return apiTokenProvider.decryptApiToken(token)
                .map(user -> new UsernamePasswordAuthenticationToken(user, null, null))
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)));

    }

}