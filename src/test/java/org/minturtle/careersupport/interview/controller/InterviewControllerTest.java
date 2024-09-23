package org.minturtle.careersupport.interview.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.minturtle.careersupport.interview.dto.InterviewTemplateResponse;
import org.minturtle.careersupport.interview.entity.InterviewTemplate;
import org.minturtle.careersupport.interview.repository.InterviewTemplateRepository;
import org.minturtle.careersupport.testutils.IntegrationTest;
import org.minturtle.careersupport.user.entity.User;
import org.minturtle.careersupport.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.assertj.core.api.Assertions.*;



class InterviewControllerTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewTemplateRepository interviewTemplateRepository;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
        interviewTemplateRepository.deleteAll().block();
    }

    @Test
    @DisplayName("로그인이 완료된 사용자는 자신이 지금까지 했던 면접 주제를 조회할 수 있다.")
    public void testUserGetInterviewTemplate() throws Exception{
        //given
        String userId = "123";
        String nickname = "nickname";
        String username = "username";
        String password = "password";

        User user = new User(userId, nickname, username, password);
        List<InterviewTemplate> givenInterviewTemplates = List.of(
                InterviewTemplate.builder().userId(userId).theme("theme1").build(),
                InterviewTemplate.builder().userId(userId).theme("theme2").build()
        );

        userRepository.save(user).block();
        interviewTemplateRepository.saveAll(givenInterviewTemplates).blockLast();

        //when
        String jwtToken = createJwtToken(user);

        List<InterviewTemplateResponse> actual = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/interview/templates")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InterviewTemplateResponse.class)
                .returnResult()
                .getResponseBody();
        //then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(givenInterviewTemplates.stream().map(InterviewTemplateResponse::of).toList());
    }


}