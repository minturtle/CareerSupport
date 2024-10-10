package org.minturtle.careersupport.common.utils;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class Base64UtilsTest {

    private static final String TEST_PYTHON_CODE;
    private static final String TEST_JS_CODE;

    private Base64Utils base64Utils = new Base64Utils();

    @ParameterizedTest
    @MethodSource("getEncodeDecodeTestData")
    @DisplayName("Base 64로 특정 문자열을 인코딩할 수 있다.")
    void testBase64Encode(String str, String expected) throws Exception{
        // when
        String actual = base64Utils.encode(str);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("getEncodeDecodeTestData")
    @DisplayName("Base 64로 특정 문자열을 디코딩할 수 있다.")
    void testBase64Decode(String expected, String str){
        // when
        String actual = base64Utils.decode(str);

        // then
        assertThat(actual).isEqualTo(expected);
    }



    private static Stream<Arguments> getEncodeDecodeTestData(){
        return Stream.of(
                Arguments.of("abcd1234!", "YWJjZDEyMzQh"),
                Arguments.of("abcd1234! ", "YWJjZDEyMzQhIA=="),
                Arguments.of(TEST_PYTHON_CODE, "IyBUaGlzIGlzIGEgc2FtcGxlIFB5dGhvbiBmaWxlCgpkZWYgaGVsbG9fd29ybGQoKToKICAgIHByaW50KCJIZWxsbywgV29ybGQhIikKCmlmIF9fbmFtZV9fID09ICJfX21haW5fXyI6CiAgICBoZWxsb193b3JsZCgp"),
                Arguments.of(TEST_JS_CODE, "Ly8gVGhpcyBpcyBhIHNhbXBsZSBKYXZhU2NyaXB0IGZpbGUKCmZ1bmN0aW9uIGhlbGxvV29ybGQoKSB7CiAgY29uc29sZS5sb2coIkhlbGxvLCBXb3JsZCEiKTsKfQoKaGVsbG9Xb3JsZCgpOw==")

        );

    }

    static{
        TEST_PYTHON_CODE = """
# This is a sample Python file

def hello_world():
    print("Hello, World!")

if __name__ == "__main__":
    hello_world()""";

        TEST_JS_CODE = """   
// This is a sample JavaScript file
                
function helloWorld() {
  console.log("Hello, World!");
}
                
helloWorld();""";
    }
}