package org.minturtle.careersupport.interview.controller

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.minturtle.careersupport.common.dto.CursoredResponse
import org.minturtle.careersupport.interview.dto.CreateInterviewTemplateResponse
import org.minturtle.careersupport.interview.dto.InterviewMessageResponse
import org.minturtle.careersupport.interview.dto.InterviewProcessRequest
import org.minturtle.careersupport.interview.dto.InterviewTemplateResponse
import org.minturtle.careersupport.interview.entity.InterviewMessage
import org.minturtle.careersupport.interview.service.InterviewService
import org.minturtle.careersupport.user.dto.UserInfoDto
import org.minturtle.careersupport.user.resolvers.annotations.CurrentUser
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/interview")
class InterviewController(
    private val interviewService: InterviewService
) {

    @GetMapping("/templates")
    suspend fun getTemplatesByUserId(
        @CurrentUser userInfo: UserInfoDto,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): List<InterviewTemplateResponse> {
        return interviewService.getTemplatesByUserId(userInfo.id, page, size)
    }


    @GetMapping("/messages")
    suspend fun getMessagesByTemplateId(
        @RequestParam templateId: String,
        @RequestParam(required = false) messageId: String?,
        @RequestParam(defaultValue = "10") size: Int
    ): CursoredResponse<InterviewMessageResponse> {
        val messages = interviewService
            .getMessagesByTemplateIdWithMessageIdCursor(templateId, messageId, size + 1)

        return CursoredResponse(
            cursor = messages.getOrNull(size)?.id,
            data = messages.take(size).reversed()
        )
    }

    @PostMapping("/new")
    suspend fun newInterview(
        @RequestParam(required = true) theme: String,
        @CurrentUser userInfo: UserInfoDto
    ): CreateInterviewTemplateResponse {
        return interviewService.createTemplate(userInfo.id, theme)
    }

    @PostMapping(value = ["/start/{templateId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun startAIInterview(
        @PathVariable templateId: String
    ): Flow<String> = coroutineScope {
        val interviewMessageFlow = interviewService.getInterviewQuestion(templateId)
            .shareIn(
                scope = this,
                started = SharingStarted.Eagerly,
                replay = 0
            )

        launch {
            val fullMessage = interviewMessageFlow
                .fold(StringBuilder()) { acc, chunk ->
                    acc.append(chunk)
                }
                .toString()

            interviewService.saveMessage(
                content = fullMessage,
                templateId = templateId,
                sender = InterviewMessage.SenderType.INTERVIEWER
            )
        }

        // 클라이언트로의 스트리밍
        interviewMessageFlow
    }

    @PostMapping(value = ["/answer/{templateId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun doAIInterview(
        @PathVariable templateId: String,
        @RequestBody reqBody: InterviewProcessRequest
    ): Flow<String> = coroutineScope {

        launch {
            // 비동기로 사용자의 메시지를 저장
            interviewService.saveMessage(
                templateId = templateId,
                sender = InterviewMessage.SenderType.USER,
                content =  reqBody.answer
            )
        }

        val followMessageBuilder = StringBuilder()

        interviewService.getFollowQuestion(templateId, reqBody.answer)
            .onEach { followMessageBuilder.append(it) }
            .onCompletion {
                // 완료시 AI의 꼬리질문도 저장
                interviewService.saveMessage(
                    content = followMessageBuilder.toString(),
                    sender = InterviewMessage.SenderType.INTERVIEWER,
                    templateId = templateId
                )
            }

    }


}