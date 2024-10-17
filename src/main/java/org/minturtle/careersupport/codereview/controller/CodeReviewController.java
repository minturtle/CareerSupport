package org.minturtle.careersupport.codereview.controller;


import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.codereview.service.AiCodeReviewClient;
import org.minturtle.careersupport.codereview.service.CodeReviewService;
import org.minturtle.careersupport.common.dto.CommonResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/code-review")
@RequiredArgsConstructor
public class CodeReviewController {

    private final CodeReviewService codeReviewService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Mono<CommonResponseBody<List<AiCodeReviewClient.ReviewResponse>>> doCodeReview(@RequestBody CodeReviewRequest codeReviewRequest) {
        return codeReviewService.doCodeReview(codeReviewRequest)
                .collectList()
                .map(CommonResponseBody::new);
    }
}
