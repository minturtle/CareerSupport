package org.minturtle.careersupport.auth.filter

import io.jsonwebtoken.JwtException
import org.minturtle.careersupport.auth.utils.JwtTokenProvider
import org.minturtle.careersupport.user.dto.UserInfoDto
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtTokenProvider
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val authorizationHeaderValue = exchange.request.headers.getFirst("Authorization")

        if (authorizationHeaderValue == null || !authorizationHeaderValue.startsWith("Bearer ")) {
            return chain.filter(exchange)
        }

        val token = authorizationHeaderValue.substring(7)

        return Mono.just(
            jwtUtil.verify(
                token
            )
        )
            .map { user: UserInfoDto? ->
                UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    null
                )
            }
            .flatMap { auth: UsernamePasswordAuthenticationToken? ->
                chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
            }
            .onErrorResume(
                JwtException::class.java
            ) { chain.filter(exchange) }
    }
}