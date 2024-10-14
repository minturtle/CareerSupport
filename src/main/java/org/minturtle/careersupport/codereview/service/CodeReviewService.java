package org.minturtle.careersupport.codereview.service;

import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import reactor.core.publisher.Mono;

public interface CodeReviewService {

    Mono<Void> doCodeReview(CodeReviewRequest codeReviewRequest);
}
