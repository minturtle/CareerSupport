package org.minturtle.careersupport.auth.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.minturtle.careersupport.common.aop.Logging
import org.minturtle.careersupport.common.exception.BadRequestException
import org.minturtle.careersupport.common.exception.InternalServerException
import org.minturtle.careersupport.common.utils.EncryptUtils
import org.minturtle.careersupport.user.dto.UserInfoDto
import org.springframework.stereotype.Component


@Logging
@Component
class ApiTokenProvider(
    private val encryptUtils: EncryptUtils,
    private val objectMapper: ObjectMapper
){

    fun generate(userInfoDto: UserInfoDto): String {
        return try {
            "cs_" + encryptUtils.encrypt(objectMapper.writeValueAsString(userInfoDto))
        } catch (e: JsonProcessingException) {
            throw InternalServerException("API 토큰 생성 중 예상치 못한 오류가 발생했습니다.", e)
        }
    }

    fun decryptApiToken(token: String): UserInfoDto {
         if (!token.startsWith("cs_")) {
            throw BadRequestException("토큰이 올바르지 않습니다.")
        }

        val decrypted = encryptUtils.decrypt(token.substring(3))

        return runCatching {
            objectMapper.readValue(decrypted, UserInfoDto::class.java)
        }.onFailure {
            throw InternalServerException("API 토큰 해독 중 예상치 못한 오류가 발생했습니다.")
        }.getOrThrow()

    }

}