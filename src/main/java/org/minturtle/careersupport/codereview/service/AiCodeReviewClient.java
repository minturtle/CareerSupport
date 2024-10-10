package org.minturtle.careersupport.codereview.service;

import reactor.core.publisher.Mono;

public interface AiCodeReviewClient {

    Mono<String> getAiCodeReview(String fileName, String fileContent);
}
