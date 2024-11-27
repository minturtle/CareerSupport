package org.minturtle.careersupport.auth.filter

import org.minturtle.careersupport.auth.utils.ApiTokenProvider
import org.minturtle.careersupport.user.dto.UserInfoDto
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


@Component
class ApiTokenFilter(
    private val apiTokenProvider: ApiTokenProvider
) : WebFilter {

    private val API_TOKEN_HEADER = "X-API-TOKEN"

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = exchange.request.headers.getFirst(API_TOKEN_HEADER)

        if (token == null || !token.startsWith("cs_")) {
            return chain.filter(exchange)
        }


        return Mono.just(apiTokenProvider.decryptApiToken(token))
            .map { user: UserInfoDto? ->
                UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    null
                )
            }
            .flatMap { auth: UsernamePasswordAuthenticationToken ->
                chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
            }

    }
}