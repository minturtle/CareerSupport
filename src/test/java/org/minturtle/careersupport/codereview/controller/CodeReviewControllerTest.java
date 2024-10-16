package org.minturtle.careersupport.codereview.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.*;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.common.facade.GithubPullRequestFacade;
import org.minturtle.careersupport.testutils.IntegrationTest;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.entity.User;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

class CodeReviewControllerTest extends IntegrationTest {


    @Test
    @DisplayName("Code Review에 필요한 Request Body를 코드리뷰를 요청할 수 있다.")
    void testCodeReviewApiCall() throws Exception{
        // given
        User user = createUser();
        String apiToken = apiTokenProvider.generate(UserInfoDto.of(user)).block();
        String ghpToken = "ghp_asdasdsaf21321";
        String repositoryName = "minturtle/careersupport";
        int prNumber = 1;

        List<GHPullRequestFileDetail> fakeGithubReqFileDetail = List.of(
                 mock(GHPullRequestFileDetail.class)
        );

        GithubPullRequestFacade mockPrFacade = mock(GithubPullRequestFacade.class);

        given(githubUtils.generatePullRequest(ghpToken, repositoryName, prNumber))
                .willReturn(mockPrFacade);
        given(mockPrFacade.getChangedFiles()).willReturn(fakeGithubReqFileDetail);
        willDoNothing().given(mockPrFacade).comment(any());

        given(fakeGithubReqFileDetail.get(0).getFilename()).willReturn("test.java");
        given(fakeGithubReqFileDetail.get(0).getStatus()).willReturn("modified");
        given(fakeGithubReqFileDetail.get(0).getPatch()).willReturn("122");

        given(chatService.generate(any(), any())).willReturn(Flux.just("AI", "댓글", "테스트"));


        CodeReviewRequest codeReviewRequestBody = CodeReviewRequest.builder()
                .repositoryName(repositoryName)
                .prNumber(prNumber)
                .githubToken(ghpToken)
                .build();


        // when & then
        webTestClient.post()
                .uri("/api/code-review")
                .header("X-API-TOKEN", apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(codeReviewRequestBody)
                .exchange()
                .expectStatus().isOk();

        verify(mockPrFacade, times(1)).getChangedFiles();
        verify(mockPrFacade, times(fakeGithubReqFileDetail.size())).comment(any());
    }




}