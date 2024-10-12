package org.minturtle.careersupport.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.minturtle.careersupport.common.exception.BadRequestException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class ReactiveEncryptUtilsTest {

    private final ReactiveEncryptUtils encryptUtils = new ReactiveEncryptUtils(
            "X1XnwJ2Vrdw9wqdfX0rOdLfNJ8rwrvB9",
            "AES/GCM/NoPadding",
            12,
            16
    );

    private final String testData = "Hello, World!";

    @Test
    @DisplayName("데이터를 Encrypt하고, Decrypt해서 데이터를 복구할 수 있다.")
    void encryptAndDecrypt() {
        Mono<String> encryptedData = encryptUtils.encrypt(testData);
        Mono<String> decryptedData = encryptedData.flatMap(encryptUtils::decrypt);

        StepVerifier.create(decryptedData)
                .expectNext(testData)
                .verifyComplete();
    }

    @Test
    @DisplayName("같은 문자열을 암호화하더라도, 다른 암호화 결과를 받을 수 있다.")
    void encryptProducesDifferentOutputForSameInput() {
        Mono<String> encrypted1 = encryptUtils.encrypt(testData);
        Mono<String> encrypted2 = encryptUtils.encrypt(testData);

        StepVerifier.create(Mono.zip(encrypted1, encrypted2))
                .assertNext(tuple -> assertThat(tuple.getT1()).isNotEqualTo(tuple.getT2()))
                .verifyComplete();
    }

    @Test
    @DisplayName("잘못된 데이터를 decrypt할 시 BadRequestException을 throw한다.")
    void decryptFailsForInvalidData() {
        Mono<String> decryptedData = encryptUtils.decrypt("InvalidEncryptedData");

        StepVerifier.create(decryptedData)
                .expectError(BadRequestException.class)
                .verify();
    }


}