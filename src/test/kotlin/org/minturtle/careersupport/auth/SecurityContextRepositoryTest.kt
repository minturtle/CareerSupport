package org.minturtle.careersupport.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.minturtle.careersupport.auth.utils.ApiTokenProvider
import org.minturtle.careersupport.auth.utils.JwtTokenProvider
import org.minturtle.careersupport.common.utils.EncryptUtils
import org.minturtle.careersupport.user.dto.UserInfoDto
import org.minturtle.careersupport.user.entity.User
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.Date

class SecurityContextRepositoryTest{

    private val authenticationManager : ReactiveAuthenticationManager = mockk()
    private val securityContextRepository = SecurityContextRepository(authenticationManager)
    private val jwtTokenProvider = JwtTokenProvider("SECRETSECRETSECRETSECRETSECRETSECRETSECRETSECRETSECRETSECRETSECRETSECRET", 100000L)
    private val objectMapper = jacksonObjectMapper()
    private val encryptUtils = EncryptUtils("gwIYgK2kSV3RM7FT3ORbNUm3Mp0lWalD")
    private val apiTokenProvider = ApiTokenProvider(encryptUtils, objectMapper)

    @Test
    fun `Authorization Header로 부터 JWT 토큰을 읽어 authenticationManager에게 전달할 수 있다`(){
        // given
        val userInfo = UserInfoDto("id", "nickname", "username")

        val jwt = jwtTokenProvider.sign(userInfo, Date())

        every { authenticationManager.authenticate(any()) } returns Mono.just(UsernamePasswordAuthenticationToken(userInfo, null, null))
        // when & then
        val exchange = createExchange(mapOf(
            HttpHeaders.AUTHORIZATION to listOf("Bearer $jwt")
        ))

        StepVerifier.create(securityContextRepository.load(exchange))
            .assertNext {
                verify(exactly = 1) { authenticationManager.authenticate(UsernamePasswordAuthenticationToken(jwt, SecurityContextRepository.TokenType.JWT)) }
            }
            .verifyComplete()


    }

    @Test
    fun `X-API-TOKEN Header로 부터 API 토큰을 읽어 authenticationManager에게 전달할 수 있다`(){
        val userInfo = UserInfoDto("id", "nickname", "username")

        val apiToken = apiTokenProvider.generate(userInfo)

        every { authenticationManager.authenticate(any()) } returns Mono.just(UsernamePasswordAuthenticationToken(userInfo, null, null))
        val exchange = createExchange(mapOf(
            "X-API-TOKEN" to listOf(apiToken)
        ))

        StepVerifier.create(securityContextRepository.load(exchange))
            .assertNext {
                verify(exactly = 1) { authenticationManager.authenticate(UsernamePasswordAuthenticationToken(apiToken, SecurityContextRepository.TokenType.API_TOKEN)) }
            }
            .verifyComplete()
    }


    private fun createExchange(headers: Map<String, List<String>>): MockServerWebExchange {
        val builder = MockServerHttpRequest.get("/")
        headers.forEach { (name, values) ->
            values.forEach { value ->
                builder.header(name, value)
            }
        }
        return MockServerWebExchange.from(builder.build())
    }
}