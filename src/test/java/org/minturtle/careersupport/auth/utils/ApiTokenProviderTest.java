package org.minturtle.careersupport.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.minturtle.careersupport.common.exception.BadRequestException;
import org.minturtle.careersupport.common.utils.ReactiveEncryptUtils;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;

class ApiTokenProviderTest {

    private ReactiveEncryptUtils reactiveEncryptUtils =
            new ReactiveEncryptUtils("X1XnwJ2Vrdw9wqdfX0rOdLfNJ8rwrvB9");

    private ObjectMapper objectMapper =
            new ObjectMapper();
    private ApiTokenProvider apiTokenProvider =
            new ApiTokenProvider(reactiveEncryptUtils, objectMapper);


    @Test
    @DisplayName("API 토큰을 생성할 수 있다.")
    void testGenerateShouldReturnNonEmptyToken() {
        UserInfoDto testUserInfo = new UserInfoDto("abc123", "nickname", "username");
        Mono<String> tokenMono = apiTokenProvider.generate(testUserInfo);

        StepVerifier.create(tokenMono)
                .assertNext(token -> {
                    assertThat(token).isNotBlank();
                    assertThat(token).startsWith("cs_");
                    assertThat(token.length()).isGreaterThan(3);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("생성한 토큰을 해독해서 userId를 반환받을 수 있다.")
    void testDecryptApiTokenShouldReturnOriginalUserId() {
        UserInfoDto testUserInfo = new UserInfoDto("abc123", "nickname", "username");
        Mono<String> tokenMono = apiTokenProvider.generate(testUserInfo);

        StepVerifier.create(tokenMono)
                .assertNext(token -> {
                    Mono<UserInfoDto> decryptedUserIdMono = apiTokenProvider.decryptApiToken(token);

                    StepVerifier.create(decryptedUserIdMono)
                            .assertNext(userInfoDto -> assertThat(userInfoDto).isEqualTo(new UserInfoDto("abc123", "nickname", "username")))
                            .verifyComplete();
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid", "cs_invalid"})
    @DisplayName("API 토큰이 잘못된 경우 BadRequestException을 throw한다.")
    public void testDecryptThrowsBadRequestException(String invalidToken) throws Exception{
        Mono<UserInfoDto> decryptedUserIdMono = apiTokenProvider.decryptApiToken(invalidToken);

        StepVerifier.create(decryptedUserIdMono)
                .expectError(BadRequestException.class)
                .verify();

    }

}