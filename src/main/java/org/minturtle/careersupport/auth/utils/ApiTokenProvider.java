package org.minturtle.careersupport.auth.utils;

import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.common.exception.BadRequestException;
import org.minturtle.careersupport.common.utils.ReactiveEncryptUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class ApiTokenProvider {

    private final ReactiveEncryptUtils encryptUtils;


    public Mono<String> generate(String userId){
        return encryptUtils.encrypt(userId)
                .map(it-> "cs_" + it);
    }

    public Mono<String> decryptApiToken(String token){
        if (token == null || !token.startsWith("cs_")) {
            return Mono.error(new BadRequestException("토큰이 올바르지 않습니다."));
        }
        return encryptUtils.decrypt(token.substring(3));
    }
}