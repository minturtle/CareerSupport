package org.minturtle.careersupport.codereview.controller

import org.minturtle.careersupport.codereview.dto.CodeReviewRequest
import org.minturtle.careersupport.codereview.dto.CodeReviewResponse
import org.minturtle.careersupport.codereview.service.GithubCodeReviewService
import org.minturtle.careersupport.common.dto.CommonResponseBody
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/api/code-review")
class CodeReviewController(
    private val codeReviewService: GithubCodeReviewService
) {


    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    suspend fun doCodeReview(@RequestBody codeReviewRequest: CodeReviewRequest): CommonResponseBody<List<CodeReviewResponse>> {
        val codeReviewResult = codeReviewService.doCodeReview(codeReviewRequest)

        return CommonResponseBody(codeReviewResult)
    }
}