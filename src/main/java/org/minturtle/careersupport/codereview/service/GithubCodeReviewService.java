package org.minturtle.careersupport.codereview.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHPullRequestReviewEvent;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.codereview.service.AiCodeReviewClient.ReviewRequest;
import org.minturtle.careersupport.codereview.service.AiCodeReviewClient.ReviewResponse;
import org.minturtle.careersupport.common.facade.GithubPullRequestFacade;
import org.minturtle.careersupport.common.utils.GithubUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
public class GithubCodeReviewService implements CodeReviewService {
    private final AiCodeReviewClient codeReviewClient;
    private final GithubUtils githubUtils;
    @Override
    public Mono<Void> doCodeReview(CodeReviewRequest codeReviewRequest) {
        String token = codeReviewRequest.getGithubToken();
        String repositoryName = codeReviewRequest.getRepositoryName();
        int prNumber = codeReviewRequest.getPrNumber();

        return Mono.just(githubUtils.generatePullRequest(token, repositoryName, prNumber))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(pullRequest -> Flux.fromIterable(pullRequest.getChangedFiles())
                        .filter(GithubCodeReviewService::isJava)
                        .map(ReviewRequest::from)
                        .transform(codeReviewClient::getAiCodeReview)
                        .flatMap(reviewComment ->
                                Mono.fromRunnable(() -> pullRequest.comment(reviewComment.getReviewContent()))
                                .subscribeOn(Schedulers.boundedElastic())
                        ).then()
                );
    }

    private static boolean isJava(GHPullRequestFileDetail ghPullRequestFileDetail) {
        return StringUtils.getFilenameExtension(ghPullRequestFileDetail.getFilename()).equals("java");
    }

}