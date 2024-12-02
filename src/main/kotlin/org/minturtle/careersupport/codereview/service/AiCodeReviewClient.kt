package org.minturtle.careersupport.codereview.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingle
import org.minturtle.careersupport.codereview.dto.CodeReviewFileInfo
import org.minturtle.careersupport.codereview.dto.CodeReviewResponse
import org.minturtle.careersupport.common.service.ChatService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class AiCodeReviewClient(
    private val chatService: ChatService,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.ai.openai.messages.code-review-system-message}")
    private val codeReviewSystemMessage: String
){

    suspend fun getAiCodeReview(file: CodeReviewFileInfo): CodeReviewResponse {
        val reviewContent = chatService.generate(codeReviewSystemMessage, objectMapper.writeValueAsString(file))
            .collect(Collectors.joining())
            .awaitSingle()

        return CodeReviewResponse(file.fileName, reviewContent)
    }
}