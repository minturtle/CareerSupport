package org.minturtle.careersupport.codereview.service;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHCommit.File;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.codereview.entity.CommitPinpoint;
import org.minturtle.careersupport.codereview.respository.ReviewPinpointRepository;
import org.minturtle.careersupport.codereview.service.AiCodeReviewClient.ReviewRequest;
import org.minturtle.careersupport.codereview.service.AiCodeReviewClient.ReviewResponse;
import org.minturtle.careersupport.common.facade.GithubPullRequestFacade;
import org.minturtle.careersupport.common.utils.GithubUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import reactor.util.context.ContextView;

@Service
@Slf4j
public class GithubCodeReviewService implements CodeReviewService {
    private static final String CONTEXT_KEY = "commitPinpoint";
    private final AiCodeReviewClient codeReviewClient;
    private final GithubUtils githubUtils;
    private final ReviewPinpointRepository reviewPinpointRepository;

    private final List<String> codeReviewWhiteList;

    public GithubCodeReviewService(
            AiCodeReviewClient codeReviewClient,
            GithubUtils githubUtils,
            @Autowired ReviewPinpointRepository reviewPinpointRepository,
            @Value("${code-review.whitelist.extensions}") List<String> codeReviewWhiteList
    ) {
        this.codeReviewClient = codeReviewClient;
        this.githubUtils = githubUtils;
        this.reviewPinpointRepository = reviewPinpointRepository;
        this.codeReviewWhiteList = codeReviewWhiteList;
    }

    @Override
    public Flux<AiCodeReviewClient.ReviewResponse> doCodeReview(CodeReviewRequest codeReviewRequest) {
        String token = codeReviewRequest.getGithubToken();
        String repositoryName = codeReviewRequest.getRepositoryName();
        int prNumber = codeReviewRequest.getPrNumber();

        return Mono.just(githubUtils.generatePullRequest(token, repositoryName, prNumber))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(pullRequest -> getReviewResponseFromAi(prNumber, repositoryName, pullRequest));
    }

    private Flux<ReviewResponse> getReviewResponseFromAi(int prNumber, String repositoryName, GithubPullRequestFacade pullRequest) {
        return reviewPinpointRepository.findByPrNumberAndRepositoryName(prNumber,repositoryName)
                .flatMapMany(commitPinpoint ->
                        pullRequest.getCommitsDiffAfter(commitPinpoint.getLastSha())
                                .contextWrite(context -> context.put(CONTEXT_KEY, commitPinpoint))
                )
                .switchIfEmpty(
                        pullRequest.getCommitsDiff()
                )
                .collectList()
                .flatMap(files ->
                        Mono.deferContextual(contextView -> saveOrUpdate(prNumber, files, contextView))
                )
                .flatMapMany(Flux::fromIterable)
                .filter(this::filterWhiteList)
                .map(ReviewRequest::from)
                .flatMap(codeReviewClient::getAiCodeReview);

    }

    private Mono<List<File>> saveOrUpdate(int prNumber, List<File> files, ContextView contextView) {
        if (contextView.hasKey(CONTEXT_KEY)) {
            CommitPinpoint commitPinpoint = contextView.get(CONTEXT_KEY);
            return updatePinPoint(commitPinpoint, files).thenReturn(files);
        } else {
            return savePinPoint(prNumber, files).thenReturn(files);
        }
    }

    /**
     * 기존의 CommitPinpoint를 업데이트합니다.
     *
     * @param existingPinpoint 기존의 CommitPinpoint
     * @param files            새로운 파일 리스트
     */
    private Mono<Void> updatePinPoint(CommitPinpoint existingPinpoint, List<File> files) {
        String newSha = files.get(files.size() - 1).getSha();
        existingPinpoint.upDateSha(newSha);
        return reviewPinpointRepository.save(existingPinpoint).then();
    }

    /**
     * 새로운 CommitPinpoint를 마지막 커밋 sha와 함께 저장합니다.
     *
     * @param prNumber PR 번호
     * @param files    파일 리스트
     */
    private Mono<Void> savePinPoint(int prNumber, List<File> files) {
        String lastCommitSha = files.get(files.size() - 1).getSha();
        CommitPinpoint commitPinpoint = CommitPinpoint.builder()
                .lastSha(lastCommitSha)
                .prNumber(prNumber)
                .build();
        return reviewPinpointRepository.save(commitPinpoint).then();
    }

    private boolean filterWhiteList(File file) {
        return codeReviewWhiteList.contains(StringUtils.getFilenameExtension(file.getFileName()));
    }
}
