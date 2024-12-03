package org.minturtle.careersupport.auth

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SecurityContextRepository(
    private val authenticationManager: ReactiveAuthenticationManager
) : ServerSecurityContextRepository {

    override fun save(exchange: ServerWebExchange, context: SecurityContext): Mono<Void> {
        // jwt는 저장할 필요 없음.
        return Mono.empty()
    }

    /**
     * JWT 또는 API Token을 Header로 부터 추출해서 AuthenticationHeader로 반환합니다.
     * 참고 : https://ard333.medium.com/authentication-and-authorization-using-jwt-on-spring-webflux-29b81f813e78
     * @author minseok kim
    */
    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        return Mono.justOrEmpty(getAuthenticateToken(exchange))
            .flatMap { (token, type) ->
                val auth: Authentication = UsernamePasswordAuthenticationToken(token, type)
                this.authenticationManager.authenticate(auth).map { SecurityContextImpl(it) }
            }
    }

    private fun getAuthenticateToken(exchange: ServerWebExchange) : Pair<String, TokenType>?{
        val jwt = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        if(jwt?.startsWith("Bearer ") == true){
            return jwt.substring(7) to TokenType.JWT
        }

        val apiToken = exchange.request.headers.getFirst("X-API-TOKEN")

        if(apiToken?.startsWith("cs_") == true){
            return apiToken to TokenType.API_TOKEN
        }

        return null
    }

    enum class TokenType{
        JWT, API_TOKEN
    }

}