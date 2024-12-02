package org.minturtle.careersupport.common.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.minturtle.careersupport.auth.utils.ApiTokenProvider
import org.minturtle.careersupport.common.exception.BadRequestException
import org.minturtle.careersupport.user.dto.UserInfoDto

class ApiTokenProviderTest {

    private val encryptUtils = EncryptUtils("X1XnwJ2Vrdw9wqdfX0rOdLfNJ8rwrvB9")

    private val objectMapper = ObjectMapper().apply {
        registerModule(
            KotlinModule.Builder()
                .configure(KotlinFeature.NullToEmptyCollection, true)
                .build()
        )
    }

    private val apiTokenProvider = ApiTokenProvider(encryptUtils, objectMapper)

    @Test
    fun `API 토큰을 생성할 수 있다`() {
        val testUserInfo = UserInfoDto("abc123", "nickname", "username")
        val token = apiTokenProvider.generate(testUserInfo)

        assertThat(token).isNotBlank()
        assertThat(token).startsWith("cs_")
        assertThat(token.length).isGreaterThan(3)
    }

    @Test
    fun `생성한 토큰을 해독해서 userId를 반환받을 수 있다`() {
        val testUserInfo = UserInfoDto("abc123", "nickname", "username")
        val token = apiTokenProvider.generate(testUserInfo)
        val userInfo = apiTokenProvider.decryptApiToken(token)

        assertThat(userInfo).isEqualTo(UserInfoDto("abc123", "nickname", "username"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "invalid", "cs_invalid"])
    fun `API 토큰이 잘못된 경우 BadRequestException을 throw한다`(invalidToken: String) {

        assertThatThrownBy {
            apiTokenProvider.decryptApiToken(invalidToken)
        }.isInstanceOf(BadRequestException::class.java)
    }
}