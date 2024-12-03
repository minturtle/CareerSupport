package org.minturtle.careersupport.auth

import org.minturtle.careersupport.auth.utils.ApiTokenProvider
import org.minturtle.careersupport.auth.utils.JwtTokenProvider
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers


@Component
class AuthenticationManager(
    private val jwtTokenProvider: JwtTokenProvider,
    private val apiTokenProvider: ApiTokenProvider
) : ReactiveAuthenticationManager{

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.principal.toString()
        val tokenType = authentication.credentials as SecurityContextRepository.TokenType

        if(tokenType == SecurityContextRepository.TokenType.JWT){
            return Mono.fromCallable { jwtTokenProvider.verify(authToken) }
                .subscribeOn(Schedulers.boundedElastic())
                .map {
                    UsernamePasswordAuthenticationToken(it, null, null)
                }
        }

        return Mono.fromCallable { apiTokenProvider.decryptApiToken(authToken) }
            .subscribeOn(Schedulers.boundedElastic())
            .map {  UsernamePasswordAuthenticationToken(it, null, null) }

    }
}