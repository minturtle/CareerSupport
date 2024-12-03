package org.minturtle.careersupport.codereview.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.ExtendWith
import org.kohsuke.github.GHCommit.File
import reactor.core.publisher.Mono
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest
import org.minturtle.careersupport.codereview.dto.CodeReviewResponse
import org.minturtle.careersupport.codereview.respository.ReviewPinpointRepository
import org.minturtle.careersupport.codereview.utils.GithubObjectFactory
import org.minturtle.careersupport.common.facade.GithubPullRequestFacade

@ExtendWith(MockKExtension::class)
class GithubCodeReviewServiceTest {
    @MockK
    lateinit var reviewPinpointRepository: ReviewPinpointRepository

    @MockK
    lateinit var codeReviewClient: AiCodeReviewClient

    @MockK
    lateinit var githubObjectFactory: GithubObjectFactory

    private val whiteList = listOf("java", "js")

    private lateinit var githubCodeReviewService: GithubCodeReviewService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        githubCodeReviewService = GithubCodeReviewService(
            codeReviewClient,
            reviewPinpointRepository,
            githubObjectFactory,
            whiteList
        )
    }

    @Test
    fun `Whitelist에 속한 확장자의 파일만 코드리뷰를 요청할 수 있다`() = runTest {
        // given
        val file1 = mockk<File>()
        val file2 = mockk<File>()
        val file3 = mockk<File>()

        val fakeFiles = listOf(file1, file2, file3)
        val codeReviewRequest = CodeReviewRequest("ghtk", "rpnm", 1)

        val mockPrFacade = mockk<GithubPullRequestFacade>()

        coEvery {
            githubObjectFactory.createFacade(
                codeReviewRequest.githubToken,
                codeReviewRequest.repositoryName,
                codeReviewRequest.prNumber
            )
        } returns mockPrFacade

        every { mockPrFacade.getCommitsDiff() } returns fakeFiles

        every { file1.fileName } returns "test.java"
        every { file1.status } returns "modified"
        every { file1.patch } returns "122"

        every { file2.fileName } returns "test.gradle"

        every { file3.fileName } returns "test.js"
        every { file3.status } returns "modified"
        every { file3.patch } returns "124"
        every { file3.sha } returns "sha"


        coEvery {
            codeReviewClient.getAiCodeReview(any())
        } returns CodeReviewResponse("filename", "comment")

        coEvery {
            reviewPinpointRepository.findByPrNumberAndRepositoryName(any(), any())
        } returns null

        coEvery {
            reviewPinpointRepository.save(any())
        } returns mockk()

        // when
        val codeReviewResponse = githubCodeReviewService.doCodeReview(codeReviewRequest)

        // then
        codeReviewResponse.forEach {
            assertThat(it.fileName).isEqualTo("filename")
            assertThat(it.reviewContent).isEqualTo("comment")
        }

        // AI 코드리뷰는 java와 js, 두개에 대해 실행되어야 한다.
        coVerify(exactly = 2) {
            codeReviewClient.getAiCodeReview(any())
        }
    }
}