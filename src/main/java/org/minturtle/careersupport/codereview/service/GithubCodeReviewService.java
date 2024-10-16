package org.minturtle.careersupport.codereview.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHPullRequestReviewEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.codereview.service.AiCodeReviewClient.ReviewRequest;
import org.minturtle.careersupport.codereview.service.AiCodeReviewClient.ReviewResponse;
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

    @Override
    public Mono<Void> doCodeReview(CodeReviewRequest codeReviewRequest) {
        String token = codeReviewRequest.getGithubToken();
        String repositoryName = codeReviewRequest.getRepositoryName();
        Long prNumber = codeReviewRequest.getPrNumber();
        return getPullRequest(token, repositoryName, prNumber)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(pullRequest -> Flux.fromIterable(pullRequest.listFiles())
                        .filter(GithubCodeReviewService::isJava)
                        .map(ReviewRequest::from)
                        .transform(codeReviewClient::getAiCodeReview)
                        .flatMap(reviewComment -> postCommentsToPullRequest(
                                pullRequest,
                                reviewComment)).then()
                );
    }

    private static boolean isJava(GHPullRequestFileDetail ghPullRequestFileDetail) {
        return StringUtils.getFilenameExtension(ghPullRequestFileDetail.getFilename()).equals("java");
    }

    private Mono<Void> postCommentsToPullRequest(GHPullRequest pullRequest, ReviewResponse reviewComment) {
        return Mono.fromCallable(() ->
                        pullRequest.createReview()
                                .comment(reviewComment.getReviewContent(), reviewComment.getFileName(), 1)
                                .event(GHPullRequestReviewEvent.COMMENT)
                                .create())
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private static Mono<GHPullRequest> getPullRequest(String token, String repositoryName, Long prNumber) {
        try {
            GitHub github = new GitHubBuilder().withOAuthToken(token).build();
            GHRepository repository = github.getRepository(repositoryName);
            return Mono.just(repository.getPullRequest(Math.toIntExact(prNumber)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
