package org.minturtle.careersupport.common.config

import org.minturtle.careersupport.auth.filter.ApiTokenFilter
import org.minturtle.careersupport.auth.filter.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.server.ServerWebExchange
import java.util.*

@Configuration
class SecurityConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Value("\${cors.origins}")
    private val origins: List<String>? = null
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter?,
        apiTokenFilter: ApiTokenFilter?
    ): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { exchanges: AuthorizeExchangeSpec ->
                exchanges
                    .pathMatchers("/api/users/register", "/api/users/login", "/api/health-check").permitAll()
                    .anyExchange().authenticated()
            }
            .cors { corsSpec: CorsSpec ->
                corsSpec.configurationSource { req: ServerWebExchange? ->
                    val config = CorsConfiguration()
                    config.allowedOrigins =
                        Arrays.asList(*origins!!.toTypedArray<String>())
                    config.setAllowedMethods(
                        mutableListOf(
                            "GET",
                            "POST",
                            "PUT",
                            "DELETE"
                        )
                    )
                    config.allowedHeaders = listOf("*")
                    config.allowCredentials = true
                    config.maxAge = 3600L
                    config
                }
            }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterAt(apiTokenFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }
}