package org.minturtle.careersupport.common.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.minturtle.careersupport.auth.utils.ApiTokenProvider;
import org.minturtle.careersupport.testutils.IntegrationTest;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;




class CommonControllerTest extends IntegrationTest {

    @Autowired
    private ApiTokenProvider apiTokenProvider;

    @Test
    public void testHealthCheck() {
        webTestClient.get().uri("/api/health-check")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("API 토큰이 필요한 api에서 currentUser의 정보를 잘 가져올 수 있다.")
    public void testGetUserInfoFromApiToken() throws Exception{
        //given
        User user = createUser();

        String apiToken = this.apiTokenProvider.generate(UserInfoDto.of(user)).block();

        //when
        Flux<String> responseBody = webTestClient.get().uri("/api/api-check")
                .header("X-API-TOKEN", apiToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .assertNext(s->assertThat(s).contains(user.getNickname()))
                .verifyComplete();

    }
}