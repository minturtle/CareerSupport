package org.minturtle.careersupport.common.utils

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.assertj.core.api.Assertions.assertThat
import java.util.stream.Stream

class Base64UtilsTest {

    @ParameterizedTest
    @MethodSource("getEncodeDecodeTestData")
    fun `Base 64로 특정 문자열을 인코딩할 수 있다`(str: String, expected: String) {
        // when
        val actual = Base64Utils.encode(str)

        // then
        assertThat(actual).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("getEncodeDecodeTestData")
    fun `Base 64로 특정 문자열을 디코딩할 수 있다`(expected: String, str: String) {
        // when
        val actual = Base64Utils.decode(str)

        // then
        assertThat(actual).isEqualTo(expected)
    }

    companion object {
        private val TEST_PYTHON_CODE = """
            # This is a sample Python file

            def hello_world():
                print("Hello, World!")

            if __name__ == "__main__":
                hello_world()
        """.trimIndent()

        private val TEST_JS_CODE = """
            // This is a sample JavaScript file
                            
            function helloWorld() {
              console.log("Hello, World!");
            }
                            
            helloWorld();
        """.trimIndent()

        @JvmStatic
        fun getEncodeDecodeTestData(): Stream<Arguments> = Stream.of(
            Arguments.of("abcd1234!", "YWJjZDEyMzQh"),
            Arguments.of("abcd1234! ", "YWJjZDEyMzQhIA=="),
            Arguments.of(TEST_PYTHON_CODE, "IyBUaGlzIGlzIGEgc2FtcGxlIFB5dGhvbiBmaWxlCgpkZWYgaGVsbG9fd29ybGQoKToKICAgIHByaW50KCJIZWxsbywgV29ybGQhIikKCmlmIF9fbmFtZV9fID09ICJfX21haW5fXyI6CiAgICBoZWxsb193b3JsZCgp"),
            Arguments.of(TEST_JS_CODE, "Ly8gVGhpcyBpcyBhIHNhbXBsZSBKYXZhU2NyaXB0IGZpbGUKCmZ1bmN0aW9uIGhlbGxvV29ybGQoKSB7CiAgY29uc29sZS5sb2coIkhlbGxvLCBXb3JsZCEiKTsKfQoKaGVsbG9Xb3JsZCgpOw==")
        )
    }
}