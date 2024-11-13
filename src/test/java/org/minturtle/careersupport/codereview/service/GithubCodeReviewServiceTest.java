package org.minturtle.careersupport.codereview.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommit.File;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GithubCodeReviewServiceTest {
    private final ReviewPinpointRepository reviewPinpointRepository = mock(ReviewPinpointRepository.class);

    private final AiCodeReviewClient mockCodeReviewClient = mock(AiCodeReviewClient.class);

    private final GithubUtils mockGithubUtils = mock(GithubUtils.class);

    private final List<String> whiteList = List.of("java", "js");
    private final GithubCodeReviewService githubCodeReviewService = new GithubCodeReviewService(mockCodeReviewClient,
            mockGithubUtils, reviewPinpointRepository, whiteList);


    @Test
    @DisplayName("Whitelist에 속한 확장자의 파일만 코드리뷰를 요청할 수 있다.")
    public void testFilterWhiteList() throws Exception {
        //given
        File file1 = mock(File.class);
        File file2 = mock(File.class);
        File file3 = mock(File.class);

        Flux<File> fakeFiles = Flux.fromIterable(List.of(file1, file2, file3));
        CodeReviewRequest codeReviewRequest = new CodeReviewRequest("ghtk", "rpnm", 1);

        GithubPullRequestFacade mockPrFacade = mock(GithubPullRequestFacade.class);

        given(mockGithubUtils.generatePullRequest(
                codeReviewRequest.getGithubToken(),
                codeReviewRequest.getRepositoryName(),
                codeReviewRequest.getPrNumber())
        ).willReturn(mockPrFacade);

        given(mockPrFacade.getCommitsDiff()).willReturn(fakeFiles);

        given(file1.getFileName()).willReturn("test.java");
        given(file1.getStatus()).willReturn("modified");
        given(file1.getPatch()).willReturn("122");

        given(file2.getFileName()).willReturn("test.gradle");

        given(file3.getFileName()).willReturn("test.js");
        given(file3.getStatus()).willReturn("modified");
        given(file3.getPatch()).willReturn("124");

        given(mockCodeReviewClient.getAiCodeReview(any())).willReturn(
                Mono.just(new AiCodeReviewClient.ReviewResponse("filename", "comment"))
        );

        given(reviewPinpointRepository.findByPrNumberAndRepositoryName(anyInt(),anyString())).willReturn(Mono.empty());
        given(reviewPinpointRepository.save(any())).willReturn(Mono.empty());

        //when & then
        // java, js에 대해 값이 반환될 것을 예상
        StepVerifier.create(githubCodeReviewService.doCodeReview(codeReviewRequest))
                .assertNext(response -> {
                    assertThat(response.getFileName()).isEqualTo("filename");
                    assertThat(response.getReviewContent()).isEqualTo("comment");
                })
                .assertNext(response -> {
                    assertThat(response.getFileName()).isEqualTo("filename");
                    assertThat(response.getReviewContent()).isEqualTo("comment");
                })
                .verifyComplete();

        // AI 코드리뷰는 java와 js, 두개에 대해 실행되어야 한다.
        verify(mockCodeReviewClient, times(2))
                .getAiCodeReview(any());
    }

}
