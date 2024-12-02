package org.minturtle.careersupport.common.utils

import org.junit.jupiter.api.Test
import org.minturtle.careersupport.common.exception.BadRequestException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy

class ReactiveEncryptUtilsTest {
    private val encryptUtils = EncryptUtils(
        "X1XnwJ2Vrdw9wqdfX0rOdLfNJ8rwrvB9"
    )

    private val testData = "Hello, World!"

    @Test
    fun `데이터를 Encrypt하고, Decrypt해서 데이터를 복구할 수 있다`() {
        val encryptedData = encryptUtils.encrypt(testData)
        val decryptedData = encryptUtils.decrypt(encryptedData)

        assertThat(decryptedData).isEqualTo(testData)
    }

    @Test
    fun `같은 문자열을 암호화하더라도, 다른 암호화 결과를 받을 수 있다`() {
        val encrypted1 = encryptUtils.encrypt(testData)
        val encrypted2 = encryptUtils.encrypt(testData)


        assertThat(encrypted1).isNotEqualTo(encrypted2)
    }

    @Test
    fun `잘못된 데이터를 decrypt할 시 BadRequestException을 throw한다`() {

        assertThatThrownBy {
            encryptUtils.decrypt("InvalidEncryptedData")
        }.isInstanceOf(BadRequestException::class.java)
    }
}