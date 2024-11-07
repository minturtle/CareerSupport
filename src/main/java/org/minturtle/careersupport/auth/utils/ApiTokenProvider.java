package org.minturtle.careersupport.auth.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.common.aop.Logging;
import org.minturtle.careersupport.common.exception.BadRequestException;
import org.minturtle.careersupport.common.exception.InternalServerException;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
@Logging
public class ApiTokenProvider {

    private final ReactiveEncryptUtils encryptUtils;
    private final ObjectMapper objectMapper;

    public Mono<String> generate(UserInfoDto userInfoDto){
        try {
            return encryptUtils.encrypt(objectMapper.writeValueAsString(userInfoDto))
                    .map(it-> "cs_" + it);
        }catch (JsonProcessingException e){
            throw new InternalServerException("API 토큰 생성 중 예상치 못한 오류가 발생했습니다.", e);
        }


    }

    public Mono<UserInfoDto> decryptApiToken(String token) {
        if (token == null || !token.startsWith("cs_")) {
            return Mono.error(new BadRequestException("토큰이 올바르지 않습니다."));
        }

        return encryptUtils.decrypt(token.substring(3)).map(decrypted -> {
            try {
                return objectMapper.readValue(decrypted, UserInfoDto.class);
            } catch (JsonProcessingException e) {
                throw new InternalServerException("API 토큰 해독 중 예상치 못한 오류가 발생했습니다.");
            }
        });

    }
}