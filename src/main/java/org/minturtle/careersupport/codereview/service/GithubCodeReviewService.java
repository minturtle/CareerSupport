package org.minturtle.careersupport.codereview.service;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.codereview.service.AiCodeReviewClient.ReviewRequest;
import org.minturtle.careersupport.common.utils.GithubUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@Slf4j
public class GithubCodeReviewService implements CodeReviewService {
    private final AiCodeReviewClient codeReviewClient;
    private final GithubUtils githubUtils;

    private final List<String> codeReviewWhiteList;

    public GithubCodeReviewService(
            AiCodeReviewClient codeReviewClient,
            GithubUtils githubUtils,
            @Value("${code-review.whitelist.extensions}") List<String> codeReviewWhiteList
    ) {
        this.codeReviewClient = codeReviewClient;
        this.githubUtils = githubUtils;
        this.codeReviewWhiteList = codeReviewWhiteList;
    }

    @Override
    public Mono<Void> doCodeReview(CodeReviewRequest codeReviewRequest) {
        String token = codeReviewRequest.getGithubToken();
        String repositoryName = codeReviewRequest.getRepositoryName();
        int prNumber = codeReviewRequest.getPrNumber();

        return Mono.just(githubUtils.generatePullRequest(token, repositoryName, prNumber))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(pullRequest -> Flux.fromIterable(pullRequest.getChangedFiles())
                        .filter(this::filterWhiteList)
                        .map(ReviewRequest::from)
                        .transform(codeReviewClient::getAiCodeReview)
                        .flatMap(reviewComment ->
                                Mono.fromRunnable(() -> pullRequest.comment(reviewComment.getReviewContent()))
                                .subscribeOn(Schedulers.boundedElastic())
                        ).then()
                );
    }

    private boolean filterWhiteList(GHPullRequestFileDetail ghPullRequestFileDetail) {
        return codeReviewWhiteList.contains(StringUtils.getFilenameExtension(ghPullRequestFileDetail.getFilename()));
    }

}