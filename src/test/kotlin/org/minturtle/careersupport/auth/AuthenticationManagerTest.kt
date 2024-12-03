package org.minturtle.careersupport.auth

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.minturtle.careersupport.auth.utils.ApiTokenProvider
import org.minturtle.careersupport.auth.utils.JwtTokenProvider
import org.minturtle.careersupport.common.utils.EncryptUtils
import org.minturtle.careersupport.user.dto.UserInfoDto
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import reactor.test.StepVerifier
import java.util.*

class AuthenticationManagerTest{

    private val jwtTokenProvider = JwtTokenProvider("SECRETSECRETSECRETSECRETSECRETSECRETSECRETSECRETSECRETSECRETSECRETSECRET", 100000L)
    private val objectMapper = jacksonObjectMapper()
    private val encryptUtils = EncryptUtils("gwIYgK2kSV3RM7FT3ORbNUm3Mp0lWalD")
    private val apiTokenProvider = ApiTokenProvider(encryptUtils, objectMapper)

    private val authenticationManager = AuthenticationManager(jwtTokenProvider, apiTokenProvider)


    @Test
    fun `authentication Manager가 JWT Token을 전달받으면 이를 Verify하여 UserInfoDto가 principal로 포함된 Authentication을 반환할 수 있다`(){
        val userInfo = UserInfoDto("id", "nickname", "username")

        val jwt = jwtTokenProvider.sign(userInfo, Date())

        val result = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(jwt, SecurityContextRepository.TokenType.JWT))

        StepVerifier.create(result)
            .assertNext { assertThat(it.principal).isEqualTo(userInfo) }
            .verifyComplete()


    }

    @Test
    fun `authentication Manager가 API Token을 전달받으면 이를 Verify하여 UserInfoDto가 principal로 포함된 Authentication을 반환할 수 있다`(){
        val userInfo = UserInfoDto("id", "nickname", "username")

        val apiToken = apiTokenProvider.generate(userInfo)

        val result = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(apiToken,
            SecurityContextRepository.TokenType.API_TOKEN
        ))

        StepVerifier.create(result)
            .assertNext { assertThat(it.principal).isEqualTo(userInfo) }
            .verifyComplete()


    }

}