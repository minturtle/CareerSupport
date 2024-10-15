package org.minturtle.careersupport.codereview.service;

import java.util.List;
import org.minturtle.careersupport.codereview.dto.github.request.ReviewComment;
import org.minturtle.careersupport.codereview.dto.github.response.PullRequestFile;
import reactor.core.publisher.Flux;

public interface AiCodeReviewClient {

    Flux<ReviewComment> getAiCodeReview(List<PullRequestFile> files);
}
