package org.minturtle.careersupport.codereview.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith
import org.kohsuke.github.GHCommit.File
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest
import org.minturtle.careersupport.codereview.dto.CodeReviewResponse
import org.minturtle.careersupport.codereview.respository.ReviewPinpointRepository
import org.minturtle.careersupport.codereview.utils.GithubObjectFactory
import org.minturtle.careersupport.common.facade.GithubPullRequestFacade
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@ExtendWith(MockitoExtension::class)
class GithubCodeReviewServiceTest {
    private val reviewPinpointRepository: ReviewPinpointRepository = mock()
    private val codeReviewClient: AiCodeReviewClient = mock()
    private val githubObjectFactory: GithubObjectFactory = mock()
    private val whiteList = listOf("java", "js")

    private val githubCodeReviewService = GithubCodeReviewService(
        codeReviewClient,
        reviewPinpointRepository,
        githubObjectFactory,
        whiteList
    )

    @Test
    fun `Whitelist에 속한 확장자의 파일만 코드리뷰를 요청할 수 있다`() = runTest{
        // given
        val file1: File = mock()
        val file2: File = mock()
        val file3: File = mock()

        val fakeFiles = listOf(file1, file2, file3)
        val codeReviewRequest = CodeReviewRequest("ghtk", "rpnm", 1)

        val mockPrFacade: GithubPullRequestFacade = mock()

        given(githubObjectFactory.createFacade(
            codeReviewRequest.githubToken,
            codeReviewRequest.repositoryName,
            codeReviewRequest.prNumber
        )).willReturn(mockPrFacade)

        given(mockPrFacade.getCommitsDiff()).willReturn(fakeFiles)

        given(file1.fileName).willReturn("test.java")
        given(file1.status).willReturn("modified")
        given(file1.patch).willReturn("122")

        given(file2.fileName).willReturn("test.gradle")

        given(file3.fileName).willReturn("test.js")
        given(file3.status).willReturn("modified")
        given(file3.patch).willReturn("124")

        given(codeReviewClient.getAiCodeReview(any())).willReturn(
            CodeReviewResponse("filename", "comment")
        )

        given(reviewPinpointRepository.findByPrNumberAndRepositoryName(anyInt(), anyString()))
            .willReturn(Mono.empty())
        given(reviewPinpointRepository.save(any())).willReturn(Mono.empty())

        // when & then
        // java, js에 대해 값이 반환될 것을 예상
        val codeReviewResponse = githubCodeReviewService.doCodeReview(codeReviewRequest)

        codeReviewResponse.forEach {
            assertThat(it.fileName).isEqualTo("filename")
            assertThat(it.reviewContent).isEqualTo("comment")

        }


        // AI 코드리뷰는 java와 js, 두개에 대해 실행되어야 한다.
        verify(codeReviewClient, times(2))
            .getAiCodeReview(any())
    }
}