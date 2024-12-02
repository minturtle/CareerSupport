package org.minturtle.careersupport.common.utils

import org.minturtle.careersupport.common.exception.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * 문자열을 암호화/ 복호화 하는 함수입니다. Blocking I/O가 발생하므로 Webflux환경에선 주의가 필요합니다.
 * @author minseok kim
*/
@Component
class EncryptUtils(
    @Value("\${spring.encryption.key}") encryptionKey: String
) {
    private val secretKey = SecretKeySpec(Base64.getDecoder().decode(encryptionKey), "AES")

    fun encrypt(data: String): String {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(ALGORITHM)
        val parameterSpec = GCMParameterSpec(
            GCM_TAG_LENGTH * 8,
            iv
        )
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
        val encryptedData =
            cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        val byteBuffer = ByteBuffer.allocate(iv.size + encryptedData.size)
        byteBuffer.put(iv)
        byteBuffer.put(encryptedData)

        return Base64.getEncoder().encodeToString(byteBuffer.array())

    }

    fun decrypt(encryptedData: String): String? {
        val decodedData = Base64.getDecoder().decode(encryptedData)
        val byteBuffer = ByteBuffer.wrap(decodedData)

        // IV 읽어오기
        val iv = ByteArray(GCM_IV_LENGTH)
        byteBuffer.get(iv)

        // 남은 데이터가 암호문
        val cipherText = ByteArray(byteBuffer.remaining())
        byteBuffer.get(cipherText)

        val cipher = Cipher.getInstance(ALGORITHM)
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

        val decryptedData = runCatching{
            cipher.doFinal(cipherText)
        }.onFailure {
            throw BadRequestException("암호문 복호화에 실패했습니다.")
        }.getOrNull()
        return decryptedData?.let { String(it, StandardCharsets.UTF_8) }
    }
    companion object {
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val ALGORITHM = "AES/GCM/NoPadding"
    }
}
