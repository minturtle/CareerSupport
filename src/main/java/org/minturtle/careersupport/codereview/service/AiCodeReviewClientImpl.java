package org.minturtle.careersupport.codereview.service;

import io.netty.util.internal.StringUtil;
import java.util.List;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.minturtle.careersupport.codereview.dto.github.request.ReviewComment;
import org.minturtle.careersupport.codereview.dto.github.request.ReviewComment.Side;
import org.minturtle.careersupport.codereview.dto.github.request.ReviewComment.SubjectType;
import org.minturtle.careersupport.codereview.dto.github.response.PullRequestFile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 테스트를 위한 서비스 구현체입니다.
 */
@Service
public class AiCodeReviewClientImpl implements AiCodeReviewClient {
    @Override
    public Flux<ReviewComment> getAiCodeReview(List<PullRequestFile> files) {
        return Flux.fromIterable(files)
                .flatMap(pullRequestFile -> {
                    String commitId = pullRequestFile.sha();
                    return Mono.just(ReviewComment.builder()
                            .commitId(commitId)
                            .body(pullRequestFile.patch())
                            .position(1)
                            .side(Side.LEFT)
                            .inReplyTo(1)
                            .line(1)
                            .startLine(1)
                            .path(pullRequestFile.filename())
                            .subjectType(SubjectType.PULL_REQUEST_REVIEW_COMMENT.toString())
                            .build());
                });
    }
}
