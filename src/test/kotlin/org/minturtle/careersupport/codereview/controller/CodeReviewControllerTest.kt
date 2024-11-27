package org.minturtle.careersupport.codereview.controller

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.kohsuke.github.GHCommit.File
import org.minturtle.careersupport.common.dto.CommonResponseBody
import org.minturtle.careersupport.user.dto.UserInfoDto
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.assertj.core.api.Assertions.assertThat
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest
import org.minturtle.careersupport.codereview.dto.CodeReviewResponse
import org.minturtle.careersupport.codereview.entity.CommitPinpoint
import org.minturtle.careersupport.codereview.respository.ReviewPinpointRepository
import org.minturtle.careersupport.common.facade.GithubPullRequestFacade
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.minturtle.careersupport.testutils.IntegrationTest

class CodeReviewControllerTest : IntegrationTest() {
    @MockBean
    private lateinit var reviewPinpointRepository: ReviewPinpointRepository

    @Test
    fun `Code Review에 필요한 Request Body를 코드리뷰를 요청할 수 있다`() = runTest {
        // given
        val user = createUser()
        val apiToken = apiTokenProvider.generate(UserInfoDto.of(user))
        val ghpToken = "ghp_asdasdsaf21321"
        val repositoryName = "minturtle/careersupport"
        val prNumber = 1

        val files = listOf(mock(File::class.java))

        val mockPrFacade = mock(GithubPullRequestFacade::class.java)
        val pinpoint = CommitPinpoint(
            lastSha = "sha",
            prNumber = prNumber,
            repositoryName = repositoryName
        )


        given(githubObjectFactory.createFacade(ghpToken, repositoryName, prNumber))
            .willReturn(mockPrFacade)

        given(mockPrFacade.getCommitsDiff())
            .willReturn(files)

        given(files[0].sha).willReturn("sha")
        given(files[0].fileName).willReturn("test.java")
        given(files[0].status).willReturn("modified")
        given(files[0].patch).willReturn("122")

        given(reviewPinpointRepository.findByPrNumberAndRepositoryName(anyInt(), anyString()))
            .willReturn(Mono.empty())
        given(reviewPinpointRepository.save(any())).willReturn(Mono.just(pinpoint))

        given(chatService.generate(any(), any())).willReturn(Flux.just("AI", "댓글", "테스트"))

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

        verify(mockPrFacade, times(1)).getCommitsDiff()
        assertThat(responseBody.data).hasSize(1)
        assertThat(responseBody.data[0]).extracting("fileName", "reviewContent")
            .containsExactly("test.java", "AI댓글테스트")
    }
}