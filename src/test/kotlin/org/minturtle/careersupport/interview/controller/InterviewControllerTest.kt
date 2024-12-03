package org.minturtle.careersupport.interview.controller

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.minturtle.careersupport.common.dto.CursoredResponse
import org.minturtle.careersupport.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import org.assertj.core.api.Assertions.assertThat
import org.minturtle.careersupport.interview.dto.CreateInterviewTemplateResponse
import org.minturtle.careersupport.interview.dto.InterviewMessageResponse
import org.minturtle.careersupport.interview.dto.InterviewProcessRequest
import org.minturtle.careersupport.interview.dto.InterviewTemplateResponse
import org.minturtle.careersupport.interview.entity.InterviewMessage
import org.minturtle.careersupport.interview.entity.InterviewTemplate
import org.minturtle.careersupport.interview.repository.InterviewMessageRepository
import org.minturtle.careersupport.interview.repository.InterviewTemplateRepository
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.minturtle.careersupport.testutils.IntegrationTest
import org.mockito.ArgumentMatchers.*
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class InterviewControllerTest : IntegrationTest() {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var interviewTemplateRepository: InterviewTemplateRepository

    @Autowired
    private lateinit var interviewMessageRepository: InterviewMessageRepository

    @BeforeEach
    fun setUp() = runTest{
        userRepository.deleteAll()
        interviewTemplateRepository.deleteAll()
        interviewMessageRepository.deleteAll()
    }

    @Test
    fun `로그인이 완료된 사용자는 자신이 지금까지 했던 면접 주제를 조회할 수 있다`() = runTest{
        // given
        val user = createUser()
        val givenInterviewTemplates = listOf(
            InterviewTemplate(userId = user.id, theme = "theme1"),
            InterviewTemplate(userId = user.id, theme = "theme2")
        )

        userRepository.save(user)
        interviewTemplateRepository.saveAll(givenInterviewTemplates)

        // when
        val jwtToken = createJwtToken(user)

        val actual = webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/interview/templates")
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(InterviewTemplateResponse::class.java)
            .returnResult()
            .responseBody

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(
            givenInterviewTemplates.map {
                InterviewTemplateResponse(it.id, it.theme, it.createdAt.atZone(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS))
            }
        )
    }

    @Test
    fun `로그인이 완료된 사용자는 특정 면접 주제에 대한 첫번째 페이지의 메시지 리스트를 조회할 수 있다`() = runTest{
        // given
        val templateId = "456"
        val user = createUser()
        val interviewTemplate = InterviewTemplate(userId = user.id, theme = "theme2")

        val interviewMessages = listOf(
            InterviewMessage(
                id = "message1",
                templateId = interviewTemplate.id,
                sender = InterviewMessage.SenderType.INTERVIEWER,
                content = "content 1"
            ),
            InterviewMessage(
                id = "message2",
                templateId = interviewTemplate.id,
                sender = InterviewMessage.SenderType.USER,
                content = "content 2"
            ),
            InterviewMessage(
                id = "message3",
                templateId = interviewTemplate.id,
                sender = InterviewMessage.SenderType.INTERVIEWER,
                content = "content 3"
            ),


        )

        userRepository.save(user)
        interviewTemplateRepository.save(interviewTemplate)
        interviewMessages.forEach { interviewMessageRepository.save(it) }

        // when
        val jwtToken = createJwtToken(user)

        val actual = webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/interview/messages")
                    .queryParam("templateId", templateId)
                    .queryParam("size", 2)
                    .build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(object : ParameterizedTypeReference<CursoredResponse<InterviewMessageResponse>>() {})
            .returnResult()
            .responseBody!!

        // then
        assertThat(actual.cursor).isEqualTo("message1")
        assertThat(actual.data).containsExactlyElementsOf(
            interviewMessages.subList(1, 3).map { InterviewMessageResponse.of(it) }
        )
    }

    @Test
    fun `사용자는 새로운 면접 주제를 생성할 수 있다`() = runTest{
        // given
        val theme = "Java Programming"
        val user = createUser()
        userRepository.save(user)

        // when
        val jwtToken = createJwtToken(user)

        val actual = webTestClient.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/interview/new")
                    .queryParam("theme", theme)
                    .build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(CreateInterviewTemplateResponse::class.java)
            .returnResult().responseBody!!

        // then
        val savedInterviewTemplate = interviewTemplateRepository.findById(actual.interviewId)!!

        assertThat(savedInterviewTemplate.userId).isEqualTo(user.id)
        assertThat(savedInterviewTemplate.theme).isEqualTo(theme)
        assertThat(actual.theme).isEqualTo(theme)
    }

    @Test
    fun `사용자는 면접 주제에 대해 새로운 면접 질문을 생성할 수 있다`() = runTest{
        // given
        val theme = "Java Programming"
        val templateId = "template001"
        val mockQuestions = Flux.just("질문", ":", "당신의 이름은?")

        val user = createUser()
        val interviewTemplate = InterviewTemplate(userId = user.id, theme = theme)

        given(chatService.generate(anyString(), anyString(), any<List<String>>()))
            .willReturn(mockQuestions)

        userRepository.save(user)
        interviewTemplateRepository.save(interviewTemplate)

        // when
        val jwtToken = createJwtToken(user)

        val resultFlux = webTestClient.post()
            .uri("/api/interview/start/{templateId}", templateId)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .exchange()
            .expectStatus().isOk
            .returnResult(String::class.java)
            .responseBody

        // then
        StepVerifier.create(resultFlux)
            .expectNext("질문")
            .expectNext(":")
            .expectNext("당신의 이름은?")
            .verifyComplete()

        verify(chatService, times(1))
            .generate(anyString(), eq(theme))

        val messages = interviewMessageRepository.findAll().toList()

        assertThat(messages).hasSize(1)
        assertThat(messages[0].content).isEqualTo("질문:당신의 이름은?")
    }

    @Test
    fun `사용자는 생성된 면접 질문에 대해 답변을 하고, 피드백을 받을 수 있다`() = runTest {
        // given
        val theme = "Java Programming"
        val templateId = "template001"
        val mockFollowQuestions = Flux.just("다음 질문", ":", "당신의 나이는?")
        val prevQuestionContent = "질문 : 당신의 이름은?"

        val user = createUser()
        val interviewTemplate = InterviewTemplate(userId = user.id, theme = theme)

        val prevQuestion = InterviewMessage(
            id = "message1",
            templateId = interviewTemplate.id,
            sender = InterviewMessage.SenderType.INTERVIEWER,
            content = "prevQuestionContent"
        )

        userRepository.save(user)
        interviewTemplateRepository.save(interviewTemplate)
        interviewMessageRepository.save(prevQuestion)

        given(chatService.generate(anyString(), anyString(), eq(listOf(prevQuestionContent))))
            .willReturn(mockFollowQuestions)

        // when
        val jwtToken = createJwtToken(user)
        val userAnswer = "제 이름은 김민석 입니다."
        val request = InterviewProcessRequest(userAnswer)

        val actual = webTestClient.post()
            .uri("/api/interview/answer/{templateId}", templateId)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .bodyValue(request)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .returnResult(String::class.java)
            .responseBody

        // then
        StepVerifier.create(actual)
            .expectNext("다음 질문")
            .expectNext(":")
            .expectNext("당신의 나이는?")
            .verifyComplete()

        verify(chatService, times(1))
            .generate(anyString(), eq(userAnswer), eq(listOf(prevQuestionContent)))

        val userMessages =
            interviewMessageRepository.findAll().filter { m -> m.sender == InterviewMessage.SenderType.USER }.toList()

        assertThat(userMessages).hasSize(1)
        assertThat(userMessages[0].content).isEqualTo("제 이름은 김민석 입니다.")
    }
}