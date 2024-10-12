package org.minturtle.careersupport.auth.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.minturtle.careersupport.common.exception.BadRequestException;
import org.minturtle.careersupport.common.utils.ReactiveEncryptUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;

class ApiTokenProviderTest {

    private ReactiveEncryptUtils reactiveEncryptUtils =
            new ReactiveEncryptUtils("X1XnwJ2Vrdw9wqdfX0rOdLfNJ8rwrvB9");
    private ApiTokenProvider apiTokenProvider =
            new ApiTokenProvider(reactiveEncryptUtils);


    @Test
    @DisplayName("API 토큰을 생성할 수 있다.")
    void testGenerateShouldReturnNonEmptyToken() {
        String testUserId = "abc123";
        Mono<String> tokenMono = apiTokenProvider.generate(testUserId);

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
        String testUserId = "abc123";
        Mono<String> tokenMono = apiTokenProvider.generate(testUserId);

        StepVerifier.create(tokenMono)
                .assertNext(token -> {
                    Mono<String> decryptedUserIdMono = apiTokenProvider.decryptApiToken(token);

                    StepVerifier.create(decryptedUserIdMono)
                            .expectNext(testUserId)
                            .verifyComplete();
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid", "cs_invalid"})
    @DisplayName("API 토큰이 잘못된 경우 BadRequestException을 throw한다.")
    public void testDecryptThrowsBadRequestException(String invalidToken) throws Exception{
        Mono<String> decryptedUserIdMono = apiTokenProvider.decryptApiToken(invalidToken);

        StepVerifier.create(decryptedUserIdMono)
                .expectError(BadRequestException.class)
                .verify();

    }

}