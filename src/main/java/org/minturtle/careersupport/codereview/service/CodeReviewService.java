package org.minturtle.careersupport.codereview.service;

import reactor.core.publisher.Mono;

public interface CodeReviewService {

    Mono<Void> doCodeReview(String repositoryName, Long prNumber, String fileName, String fileContent);
}
