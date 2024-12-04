package org.minturtle.careersupport.codereview.controller

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.kohsuke.github.GHCommit.File
import org.minturtle.careersupport.common.dto.CommonResponseBody
import org.minturtle.careersupport.user.dto.UserInfoDto
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import reactor.core.publisher.Flux
import org.assertj.core.api.Assertions.assertThat
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest
import org.minturtle.careersupport.codereview.dto.CodeReviewResponse
import org.minturtle.careersupport.common.facade.GithubPullRequestFacade
import org.minturtle.careersupport.testutils.IntegrationTest

class CodeReviewControllerTest : IntegrationTest() {

    @Test
    fun `Code Review에 필요한 Request Body를 입력해 코드리뷰를 요청할 수 있다`() = runTest {
        // given
        val user = createUser()

        val apiToken = apiTokenProvider.generate(UserInfoDto.of(user))

        val ghpToken = "ghp_asdasdsaf21321"
        val repositoryName = "minturtle/careersupport"
        val prNumber = 1

        val mockFile = mockk<File>()
        val files = listOf(mockFile)

        val mockPrFacade = mockk<GithubPullRequestFacade>()

        coEvery { githubObjectFactory.createFacade(ghpToken, repositoryName, prNumber) } returns mockPrFacade

        every { mockPrFacade.getCommitsDiff() } returns files

        every { mockFile.sha } returns "sha"
        every { mockFile.fileName } returns "test.java"
        every { mockFile.status } returns "modified"
        every { mockFile.patch } returns "122"

        every {
            chatService.generate(any(), any(), any<List<String>>())
        } returns Flux.just("AI", "댓글", "테스트")


        val codeReviewRequestBody = CodeReviewRequest(
            githubToken = ghpToken,
            repositoryName = repositoryName,
            prNumber = prNumber
        )

        // when & then
        val responseBody = webTestClient.post()
            .uri("/api/code-review")
            .header("X-API-TOKEN", apiToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(codeReviewRequestBody)
            .exchange()
            .expectStatus().isOk
            .expectBody(object : ParameterizedTypeReference<CommonResponseBody<List<CodeReviewResponse>>>() {})
            .returnResult()
            .responseBody!!

        verify(exactly = 1) { mockPrFacade.getCommitsDiff() }
        assertThat(responseBody.data).hasSize(1)
        assertThat(responseBody.data[0]).extracting("fileName", "reviewContent")
            .containsExactly("test.java", "AI댓글테스트")
    }
}