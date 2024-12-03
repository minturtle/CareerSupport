package org.minturtle.careersupport.auth

import org.minturtle.careersupport.auth.utils.JwtTokenProvider
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


@Component
class AuthenticationManager(
    private val jwtTokenProvider: JwtTokenProvider
) : ReactiveAuthenticationManager{

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.principal.toString()

        return Mono.just(jwtTokenProvider.verify(authToken))
            .map {
                UsernamePasswordAuthenticationToken(it, null, null)
            }
    }
}