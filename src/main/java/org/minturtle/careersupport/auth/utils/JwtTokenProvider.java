package org.minturtle.careersupport.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;

    private final Long ACCESS_TOKEN_EXPIRE_TIME;

    private static final String USER_USERNAME_CLAIM_NAME = "email";

    private static final String USER_NICKNAME_CLAIM_NAME = "nickname";

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expire-time}") Long ACCESS_TOKEN_EXPIRE_TIME
    ) {
        this.key =  Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.ACCESS_TOKEN_EXPIRE_TIME = ACCESS_TOKEN_EXPIRE_TIME;
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
    public String sign(UserInfoDto user, Date now) {
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .subject(user.getId())
                .claim(USER_USERNAME_CLAIM_NAME, user.getUsername())
                .claim(USER_NICKNAME_CLAIM_NAME, user.getNickname())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * methodName : verify
     * Author : minseok Kim
     * description : 엑세스 토큰의 값을 읽고 결과값을 반환하는 함수
     *
     * @param : String Token - 해독하려는 토큰
     * @return : 해독된 토큰의 uid
     */
    public UserInfoDto verify(String token) {
        Claims payload = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();


        return UserInfoDto.builder()
                .id(payload.getSubject())
                .nickname(payload.get(USER_NICKNAME_CLAIM_NAME, String.class))
                .username(payload.get(USER_USERNAME_CLAIM_NAME, String.class))
                .build();
    }


}
