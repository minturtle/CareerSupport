package org.minturtle.careersupport.auth.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.minturtle.careersupport.common.aop.Logging
import org.minturtle.careersupport.common.exception.InternalServerException
import org.minturtle.careersupport.user.dto.UserInfoDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*


@Logging
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") secretKey: String,
    @Value("\${jwt.access-token-expire-time}") private val ACCESS_TOKEN_EXPIRE_TIME : Long
) {

    private val secretKey = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

    companion object{
        private const val USER_USERNAME_CLAIM_NAME = "email"

        private const val USER_NICKNAME_CLAIM_NAME = "nickname"

    }


    /**
     * methodName : sign
     * Author : Minseok Kim
     * description : 엑세스 토큰을 생성하는 메서드
     *
     * @param : user - 토큰을 생성하려는 유저 정보
     * @param : now : token을 생성하는 시간
     * @return : 엑세스 토큰 리턴
     */
    @Logging
    fun sign(user: UserInfoDto, now: Date): String {
        val expiryDate = Date(now.time + ACCESS_TOKEN_EXPIRE_TIME)

        return Jwts.builder()
            .subject(user.id)
            .claim(USER_USERNAME_CLAIM_NAME, user.username)
            .claim(USER_NICKNAME_CLAIM_NAME, user.nickname)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * methodName : verify
     * Author : minseok Kim
     * description : 엑세스 토큰의 값을 읽고 결과값을 반환하는 함수
     *
     * @param : String Token - 해독하려는 토큰
     * @return : 해독된 토큰의 uid
     */
    fun verify(token: String): UserInfoDto {
        return runCatching {
            val payload: Claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload()

            UserInfoDto(
                payload.subject,
                payload.get(
                    USER_NICKNAME_CLAIM_NAME,
                    String::class.java
                ),
                payload.get(
                    USER_USERNAME_CLAIM_NAME,
                    String::class.java
                )
            )

        }
            .onFailure { e -> throw InternalServerException("토큰 복호화 중 오류 발생", e) }
            .getOrThrow()


    }


}