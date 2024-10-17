package org.minturtle.careersupport.codereview.service;

import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import reactor.core.publisher.Flux;

public interface CodeReviewService {

    Flux<AiCodeReviewClient.ReviewResponse> doCodeReview(CodeReviewRequest codeReviewRequest);
}
