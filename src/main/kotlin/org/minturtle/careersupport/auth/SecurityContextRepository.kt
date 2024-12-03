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
        throw UnsupportedOperationException("Not supported yet.")
    }

    /**
     * JWT 또는 API Token을 Header로 부터 추출해서 AuthenticationHeader로 반환합니다.
     * 참고 : https://ard333.medium.com/authentication-and-authorization-using-jwt-on-spring-webflux-29b81f813e78
     * @author minseok kim
    */
    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
            .filter { authHeader -> authHeader.startsWith("Bearer ") }
            .flatMap { authHeader ->
                val authToken: String = authHeader.substring(7)
                val auth: Authentication = UsernamePasswordAuthenticationToken(authToken, authToken)
                this.authenticationManager.authenticate(auth).map { SecurityContextImpl() }
            }
    }
}