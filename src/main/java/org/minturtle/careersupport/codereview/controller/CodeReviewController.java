package org.minturtle.careersupport.codereview.controller;


import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.minturtle.careersupport.codereview.service.CodeReviewService;
import org.minturtle.careersupport.common.utils.Base64Utils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/code-review")
@RequiredArgsConstructor
public class CodeReviewController {

    private final Base64Utils base64Utils;
    private final CodeReviewService codeReviewService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> doCodeReview(@RequestBody CodeReviewRequest codeReviewRequest){
        return Flux.fromIterable(codeReviewRequest.getFileContents().entrySet())
                .flatMap(entry -> {
                    String key = entry.getKey();
                    String encodedValue = entry.getValue();

                    return Mono.just(encodedValue)
                            .map(base64Utils::decode)
                            .flatMap(decodedValue ->
                                codeReviewService.doCodeReview(
                                        codeReviewRequest.getRepositoryName(),
                                        codeReviewRequest.getPrNumber(),
                                        key,
                                        decodedValue
                                )
                            );
                })
                .then();

    }
}
