package org.minturtle.careersupport.codereview.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.testutils.IntegrationTest;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.entity.User;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CodeReviewControllerTest extends IntegrationTest {


    @Test
    @DisplayName("Code Review에 필요한 Request Body를 코드리뷰를 요청할 수 있다.")
    void testCodeReviewApiCall() throws Exception{
        // given
        User user = createUser();
        String apiToken = apiTokenProvider.generate(UserInfoDto.of(user)).block();
        String ghpToken = "ghp_asdasdsaf21321";
        String repositoryName = "minturtle/careersupport";
        long prNumber = 1L;


        CodeReviewRequest codeReviewRequestBody = CodeReviewRequest.builder()
                .repositoryName(repositoryName)
                .prNumber(prNumber)
                .githubToken(ghpToken)
                .build();

        given(codeReviewService.doCodeReview(codeReviewRequestBody))
                .willReturn(Mono.empty());

        // when & then
        webTestClient.post()
                .uri("/api/code-review")
                .header("X-API-TOKEN", apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(codeReviewRequestBody)
                .exchange()
                .expectStatus().isOk();

        // TODO : 추후에 실제 빈으로 변경해서 테스트 코드도 변경해야함.
        verify(codeReviewService, times(1))
                .doCodeReview(codeReviewRequestBody);

        verify(codeReviewService, times(1))
                .doCodeReview(codeReviewRequestBody);
    }




}
