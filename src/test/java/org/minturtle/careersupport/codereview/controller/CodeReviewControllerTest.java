package org.minturtle.careersupport.codereview.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.testutils.IntegrationTest;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CodeReviewControllerTest extends IntegrationTest {

    private static final String TEST_PYTHON_CODE_ENCODED;
    private static final String TEST_JS_CODE_ENCODED;


    @Test
    @DisplayName("Code Review에 필요한 Request Body를 코드리뷰를 요청할 수 있다.")
    void testCodeReviewApiCall() throws Exception{
        // given
        CodeReviewRequest codeReviewRequestBody = CodeReviewRequest.builder()
                .repositoryName("minturtle/careersupport")
                .prNumber(1L)
                .fileContents(Map.of(
                        "example.py", TEST_PYTHON_CODE_ENCODED,
                        "example.js", TEST_JS_CODE_ENCODED
                ))
                .build();

        given(codeReviewService.doCodeReview(any(), any(), any(), any()))
                .willReturn(Mono.empty());

        // when & then
        webTestClient.post()
                .uri("/api/code-review")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(codeReviewRequestBody)
                .exchange()
                .expectStatus().isOk();

        // TODO : 추후에 실제 빈으로 변경해서 테스트 코드도 변경해야함.
        verify(codeReviewService, times(1))
                .doCodeReview(eq("minturtle/careersupport"), eq(1L), eq("example.py"), any());

        verify(codeReviewService, times(1))
                .doCodeReview(eq("minturtle/careersupport"), eq(1L), eq("example.js"), any());
    }


    static{
        TEST_PYTHON_CODE_ENCODED = "IyBUaGlzIGlzIGEgc2FtcGxlIFB5dGhvbiBmaWxlCgpkZWYgaGVsbG9fd29ybGQoKToKICAgIHByaW50KCJIZWxsbywgV29ybGQhIikKCmlmIF9fbmFtZV9fID09ICJfX21haW5fXyI6CiAgICBoZWxsb193b3JsZCgp";
        TEST_JS_CODE_ENCODED = "Ly8gVGhpcyBpcyBhIHNhbXBsZSBKYXZhU2NyaXB0IGZpbGUKCmZ1bmN0aW9uIGhlbGxvV29ybGQoKSB7CiAgY29uc29sZS5sb2coIkhlbGxvLCBXb3JsZCEiKTsKfQoKaGVsbG9Xb3JsZCgpOw==";
    }

}