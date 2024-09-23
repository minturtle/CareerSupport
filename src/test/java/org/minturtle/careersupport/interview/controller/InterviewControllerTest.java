package org.minturtle.careersupport.interview.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.minturtle.careersupport.common.dto.CursoredResponse;
import org.minturtle.careersupport.interview.dto.CreateInterviewTemplateResponse;
import org.minturtle.careersupport.interview.dto.InterviewMessageResponse;
import org.minturtle.careersupport.interview.dto.InterviewTemplateResponse;
import org.minturtle.careersupport.interview.entity.InterviewMessage;
import org.minturtle.careersupport.interview.entity.InterviewTemplate;
import org.minturtle.careersupport.interview.repository.InterviewMessageRepository;
import org.minturtle.careersupport.interview.repository.InterviewTemplateRepository;
import org.minturtle.careersupport.testutils.IntegrationTest;
import org.minturtle.careersupport.user.entity.User;
import org.minturtle.careersupport.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.assertj.core.api.Assertions.*;



class InterviewControllerTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewTemplateRepository interviewTemplateRepository;

    @Autowired
    private InterviewMessageRepository interviewMessageRepository;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
        interviewTemplateRepository.deleteAll().block();
        interviewMessageRepository.deleteAll().block();
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


    @Test
    @DisplayName("로그인이 완료된 사용자는 특정 면접 주제에 대한 첫번째 페이지의 메시지 리스트를 조회할 수 있다.")
    public void testUserGetInterviewMessage() throws Exception{
        //given
        String userId = "123";
        String nickname = "nickname";
        String username = "username";
        String password = "password";

        String templateId = "456";

        User user = new User(userId, nickname, username, password);
        InterviewTemplate interviewTemplate = InterviewTemplate.builder().id(templateId).userId(userId).theme("theme1").build();

        List<InterviewMessage> interviewMessages = List.of(
                InterviewMessage.builder()
                        .id("message1")
                        .templateId(templateId)
                        .sender(InterviewMessage.SenderType.INTERVIEWER)
                        .content("content 1")
                        .build(),
                InterviewMessage.builder()
                        .id("message2")
                        .templateId(templateId)
                        .sender(InterviewMessage.SenderType.USER)
                        .content("content 2")
                        .build(),
                InterviewMessage.builder()
                        .id("message3")
                        .templateId(templateId)
                        .sender(InterviewMessage.SenderType.INTERVIEWER)
                        .content("content 3")
                        .build()
        );



        userRepository.save(user).block();
        interviewTemplateRepository.save(interviewTemplate).block();

        for(InterviewMessage msg : interviewMessages){
            interviewMessageRepository.save(msg).block();
        }

        //when
        String jwtToken = createJwtToken(user);

        CursoredResponse<InterviewMessageResponse> actual = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/interview/messages")
                        .queryParam("templateId", templateId)
                        .queryParam("size", 2)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<CursoredResponse<InterviewMessageResponse>>() {})
                .returnResult()
                .getResponseBody();

        //then
        assertThat(actual.getCursor()).isEqualTo("message1");
        assertThat(actual.getData())
                .containsExactlyElementsOf(
                        interviewMessages.subList(1, 3).stream().map(InterviewMessageResponse::of).toList()
                );
    }

    @Test
    @DisplayName("로그인이 완료된 사용자는 특정 면접 주제에 대한 두번째 이상 페이지의 메시지 리스트를 조회할 수 있다.")
    public void testUserGetInterviewMessageNextCursor() throws Exception{
        //given
        String userId = "123";
        String nickname = "nickname";
        String username = "username";
        String password = "password";

        String templateId = "456";

        User user = new User(userId, nickname, username, password);
        InterviewTemplate interviewTemplate = InterviewTemplate.builder().id(templateId).userId(userId).theme("theme1").build();

        List<InterviewMessage> interviewMessages = List.of(
                InterviewMessage.builder()
                        .id("message1")
                        .templateId(templateId)
                        .sender(InterviewMessage.SenderType.INTERVIEWER)
                        .content("content 1")
                        .build(),
                InterviewMessage.builder()
                        .id("message2")
                        .templateId(templateId)
                        .sender(InterviewMessage.SenderType.USER)
                        .content("content 2")
                        .build(),
                InterviewMessage.builder()
                        .id("message3")
                        .templateId(templateId)
                        .sender(InterviewMessage.SenderType.INTERVIEWER)
                        .content("content 3")
                        .build()
        );



        userRepository.save(user).block();
        interviewTemplateRepository.save(interviewTemplate).block();

        for(InterviewMessage msg : interviewMessages){
            interviewMessageRepository.save(msg).block();
        }

        //when
        String jwtToken = createJwtToken(user);

        CursoredResponse<InterviewMessageResponse> actual = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/interview/messages")
                        .queryParam("templateId", templateId)
                        .queryParam("size", 2)
                        .queryParam("messageId", "message1")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<CursoredResponse<InterviewMessageResponse>>() {})
                .returnResult()
                .getResponseBody();

        //then
        assertThat(actual.getCursor()).isEqualTo(null);
        assertThat(actual.getData())
                .containsExactlyElementsOf(
                        interviewMessages.subList(0, 1).stream().map(InterviewMessageResponse::of).toList()
                );
    }

    @Test
    @DisplayName("사용자는 새로운 면접 주제를 생성할 수 있다.")
    public void testUserCreateNewInterviewTemplate() throws Exception{
        //given
        String theme = "Java Programming";
        String userId = "123";
        String nickname = "nickname";
        String username = "username";
        String password = "password";

        User user = new User(userId, nickname, username, password);

        userRepository.save(user).block();

        //when
        String jwtToken = createJwtToken(user);

        CreateInterviewTemplateResponse actual = webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/interview/new")
                        .queryParam("theme", theme)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateInterviewTemplateResponse.class)
                .returnResult().getResponseBody();

        //then
        InterviewTemplate savedInterviewTemplate = interviewTemplateRepository.findById(actual.getInterviewId()).block();

        assertThat(savedInterviewTemplate.getUserId()).isEqualTo(user.getId());
        assertThat(savedInterviewTemplate.getTheme()).isEqualTo(theme);
        assertThat(actual.getTheme()).isEqualTo(theme);

    }

}