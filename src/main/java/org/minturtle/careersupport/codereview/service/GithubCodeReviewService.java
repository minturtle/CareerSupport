package org.minturtle.careersupport.codereview.service;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestReviewEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.codereview.dto.github.request.ReviewComment;
import org.minturtle.careersupport.codereview.dto.github.response.PullRequestFile;
import org.springframework.stereotype.Service;
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
        return Mono.fromCallable(() -> {
                    GHPullRequest pullRequest = getPullRequest(token, repositoryName, prNumber);
                    return pullRequest.listFiles();
                })
                .publishOn(Schedulers.boundedElastic())
                .flatMapMany(files -> {
                    try {
                        return Flux.fromIterable(files.toList().stream()
                                        .map(PullRequestFile::from)
                                        .toList());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collectList()
                .flatMap(files -> codeReviewClient.getAiCodeReview(files)
                        .collectList()
                        .flatMap(reviewComments -> postCommentsToPullRequest(
                                token,
                                repositoryName,
                                prNumber,
                                reviewComments)))
                .then();
    }


    private Mono<Void> postCommentsToPullRequest(String token, String repositoryName, Long prNumber,
                                                 List<ReviewComment> reviewComments) {
        return Mono.fromCallable(() -> getPullRequest(token, repositoryName, prNumber))
                .publishOn(Schedulers.boundedElastic())
                .flatMapMany(pullRequest -> Flux.fromIterable(reviewComments)
                        .flatMap(comment -> Mono.fromCallable(
                                        () -> pullRequest.createReview()
                                                .singleLineComment(comment.getBody(), comment.getPath(), comment.getLine())
                                                .event(GHPullRequestReviewEvent.COMMENT)
                                                .create()
                                        )
                                        .doOnError(Throwable::printStackTrace)
                        ))
                .then();
    }

    private static GHPullRequest getPullRequest(String token, String repositoryName, Long prNumber)
            throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        GHRepository repository = github.getRepository(repositoryName);
        return repository.getPullRequest(Math.toIntExact(prNumber));
    }
}
