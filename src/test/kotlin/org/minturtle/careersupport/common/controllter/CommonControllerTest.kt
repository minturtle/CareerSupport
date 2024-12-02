package org.minturtle.careersupport.common.controller

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.minturtle.careersupport.user.dto.UserInfoDto
import reactor.test.StepVerifier
import org.assertj.core.api.Assertions.assertThat
import org.minturtle.careersupport.testutils.IntegrationTest

class CommonControllerTest : IntegrationTest() {
    @Test
    fun `health check API는 200 OK를 반환한다`() {
        webTestClient.get().uri("/api/health-check")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `API 토큰이 필요한 api에서 currentUser의 정보를 잘 가져올 수 있다`() = runTest {
        // given
        val user = createUser()
        val apiToken = apiTokenProvider.generate(UserInfoDto.of(user))

        // when
        val responseBody = webTestClient.get().uri("/api/api-check")
            .header("X-API-TOKEN", apiToken)
            .exchange()
            .expectStatus().isOk
            .returnResult(String::class.java)
            .responseBody

        // then
        StepVerifier.create(responseBody)
            .assertNext { response ->
                assertThat(response).contains(user.nickname)
            }
            .verifyComplete()
    }
}
