package org.minturtle.careersupport.codereview.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHCommit.File;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.codereview.entity.CommitPinpoint;
import org.minturtle.careersupport.codereview.respository.ReviewPinpointRepository;
import org.minturtle.careersupport.codereview.service.AiCodeReviewClient;
import org.minturtle.careersupport.common.dto.CommonResponseBody;
import org.minturtle.careersupport.common.facade.GithubPullRequestFacade;
import org.minturtle.careersupport.testutils.IntegrationTest;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.entity.User;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;


import java.util.List;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class CodeReviewControllerTest extends IntegrationTest {
    @MockBean
    ReviewPinpointRepository reviewPinpointRepository;

    @Test
    @DisplayName("Code Review에 필요한 Request Body를 코드리뷰를 요청할 수 있다.")
    void testCodeReviewApiCall() throws Exception{
        // given
        User user = createUser();
        String apiToken = apiTokenProvider.generate(UserInfoDto.of(user)).block();
        String ghpToken = "ghp_asdasdsaf21321";
        String repositoryName = "minturtle/careersupport";
        int prNumber = 1;

        File[] files = new File[]{mock(File.class)};
        Flux<File> fakeCommitsDiff = Flux.fromArray(files);

        GithubPullRequestFacade mockPrFacade = mock(GithubPullRequestFacade.class);
        CommitPinpoint pinpoint = CommitPinpoint.builder()
                .prNumber(prNumber)
                .lastSha("sha")
                .build();


        given(githubUtils.generatePullRequest(ghpToken, repositoryName, prNumber))
                .willReturn(mockPrFacade);
        given((mockPrFacade.getCommitsDiff()))
                .willReturn(fakeCommitsDiff);

        given(files[0].getSha()).willReturn("sha");
        given(files[0].getFileName()).willReturn("test.java");
        given(files[0].getStatus()).willReturn("modified");
        given(files[0].getPatch()).willReturn("122");

        given(reviewPinpointRepository.findByPrNumberAndRepositoryName(anyInt(),anyString())).willReturn(Mono.empty());
        given(reviewPinpointRepository.save(any())).willReturn(Mono.just(pinpoint));

        given(chatService.generate(any(), any())).willReturn(Flux.just("AI", "댓글", "테스트"));

        CodeReviewRequest codeReviewRequestBody = CodeReviewRequest.builder()
                .repositoryName(repositoryName)
                .prNumber(prNumber)
                .githubToken(ghpToken)
                .build();

        // when & then
        CommonResponseBody<List<AiCodeReviewClient.ReviewResponse>> responseBody = webTestClient.post()
                .uri("/api/code-review")
                .header("X-API-TOKEN", apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(codeReviewRequestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<CommonResponseBody<List<AiCodeReviewClient.ReviewResponse>>>() {
                })
                .returnResult()
                .getResponseBody();

        verify(mockPrFacade, times(1)).getCommitsDiff();
        assertThat(responseBody.getData()).hasSize(1);
        assertThat(responseBody.getData().get(0)).extracting("fileName", "reviewContent")
                .containsExactly("test.java", "AI댓글테스트");
    }
}
