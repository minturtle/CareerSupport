package org.minturtle.careersupport.auth

import kotlinx.coroutines.reactor.mono
import org.minturtle.careersupport.auth.utils.ApiTokenProvider
import org.minturtle.careersupport.auth.utils.JwtTokenProvider
import org.springframework.security.authentication.BadCredentialsException
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
        return mono<Authentication> {
            val authToken = authentication.principal.toString()
            val tokenType = authentication.credentials as SecurityContextRepository.TokenType

            if(tokenType == SecurityContextRepository.TokenType.JWT){
                val userInfoDto = jwtTokenProvider.verify(authToken)

                return@mono UsernamePasswordAuthenticationToken(userInfoDto, null, null)
            }

            val userInfoDto = apiTokenProvider.decryptApiToken(authToken)

            UsernamePasswordAuthenticationToken(userInfoDto, null, null)
        }.onErrorResume { e ->
            // Mono.Error를 반환하라고 되어 있는데.. error를 반환하면 500이 뜸. 왜 이런지 찾아야할듯(minseok)
            Mono.empty()
        }

    }
}