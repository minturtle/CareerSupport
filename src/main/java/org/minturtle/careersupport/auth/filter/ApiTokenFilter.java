package org.minturtle.careersupport.auth.filter;

import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@Component
public class ApiTokenFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // TODO: 실제 토큰 검증 및 사용자 정보 추출 로직 구현
        return Mono.just(new UserInfoDto("username", "role", "token"))
                .map(user -> new UsernamePasswordAuthenticationToken(user, null, null))
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)));

    }

}
