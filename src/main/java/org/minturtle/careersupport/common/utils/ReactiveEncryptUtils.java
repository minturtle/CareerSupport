package org.minturtle.careersupport.common.utils;


import org.minturtle.careersupport.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ReactiveEncryptUtils {

    private final SecretKey secretKey;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    public ReactiveEncryptUtils(
            @Value("${spring.encryption.key}") String encryptionKey
    ) {
        this.secretKey = new SecretKeySpec(Base64.getDecoder().decode(encryptionKey), "AES");
    }

    public Mono<String> encrypt(String data) {
        return Mono.fromCallable(() -> {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> new RuntimeException("Encryption failed : " + e.getMessage(), e));
    }

    public Mono<String> decrypt(String encryptedData) {
        return Mono.fromCallable(() -> {
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedData = cipher.doFinal(cipherText);
            return new String(decryptedData, StandardCharsets.UTF_8);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> new BadRequestException("Decryption failed" + e.getMessage(), e));
    }
}