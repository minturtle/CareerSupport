package org.minturtle.careersupport.interview.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingle
import org.minturtle.careersupport.common.aop.Logging
import org.minturtle.careersupport.common.service.ChatService
import org.minturtle.careersupport.interview.dto.CreateInterviewTemplateResponse
import org.minturtle.careersupport.interview.dto.InterviewMessageResponse
import org.minturtle.careersupport.interview.dto.InterviewTemplateResponse
import org.minturtle.careersupport.interview.entity.InterviewMessage
import org.minturtle.careersupport.interview.entity.InterviewMessage.SenderType
import org.minturtle.careersupport.interview.entity.InterviewTemplate
import org.minturtle.careersupport.interview.repository.InterviewMessageRepository
import org.minturtle.careersupport.interview.repository.InterviewTemplateRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux


@Logging
@Service
class InterviewService(
    private val chatService: ChatService,
    private val interviewTemplateRepository: InterviewTemplateRepository,
    private val interviewMessageRepository: InterviewMessageRepository,
    @Value("\${spring.ai.openai.messages.interview-system-message}")
    private val interviewSystemMessage: String,
    @Value("\${spring.ai.openai.messages.follow-system-message}")
    private val followSystemMessage: String
) {
    suspend fun getTemplatesByUserId(userId: String, page: Int, size: Int): List<InterviewTemplateResponse> {
        return interviewTemplateRepository
            .findByUserId(userId, PageRequest.of(page, size))
            .map { interviewTemplate: InterviewTemplate? -> InterviewTemplateResponse.of(interviewTemplate) }
            .collectList()
            .awaitFirstOrElse { listOf() }
    }

    suspend fun getMessagesByTemplateIdWithMessageIdCursor(
        templateId: String,
        cursor: String? = null,
        size: Int = 10
    ): List<InterviewMessageResponse> {
        val pageable: Pageable = PageRequest.of(0, size)

        val messages = getMessages(templateId, pageable, cursor)

        return messages
            .map { interviewMessage: InterviewMessage? ->
                InterviewMessageResponse.of(
                    interviewMessage
                )
            }
    }

    suspend fun createTemplate(
        userId: String, theme: String
    ): CreateInterviewTemplateResponse {
        val interviewTemplate = InterviewTemplate(
            userId = userId,
            theme = theme
        )

        val savedTemplate = interviewTemplateRepository.save(interviewTemplate).awaitSingle()

         return savedTemplate.let { CreateInterviewTemplateResponse.of(it) }
    }


    suspend fun getInterviewQuestion(templateId: String): Flow<String> {
        val template = interviewTemplateRepository.findById(templateId).awaitSingle()

        return chatService.generate(interviewSystemMessage, template.theme).asFlow()
    }

    suspend fun getFollowQuestion(templateId: String, answer: String): Flow<String> {
        val lastInteviewerMessage = interviewMessageRepository.findFirstByTemplateIdAndSenderOrderByCreatedAtDesc(
            templateId,
            SenderType.INTERVIEWER
        ).awaitSingle()!!

        return chatService.generate(
            followSystemMessage,
            answer,
            listOf(lastInteviewerMessage.content)
        )
            .asFlow()
    }

    suspend fun saveMessage(templateId: String, sender: SenderType, content: String) {
        val message = InterviewMessage(
            templateId = templateId,
            sender = sender,
            content = content
        )

        interviewMessageRepository.save(message).awaitSingle()
    }



    private suspend fun getMessages(templateId: String, pageable: Pageable, cursor: String? = null): List<InterviewMessage> {
        if(cursor.isNullOrEmpty()){
            return interviewMessageRepository
                .findTopNByTemplateIdOrderByIdDesc(templateId, pageable).collectList().awaitSingle()
        }

        return interviewMessageRepository
            .findByTemplateIdAndIdLessThanEqualOrderByIdDesc(templateId!!, cursor, pageable).collectList().awaitSingle()
    }
}