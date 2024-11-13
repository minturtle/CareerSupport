package org.minturtle.careersupport.common.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux


@Component
class ChatService(private val chatClient: ChatClient) {
    fun generate(systemMessage: String, message: String?, assistantMessages: List<String>? = null): Flux<String> {
        return chatClient.prompt()
            .system(systemMessage)
            .user(message)
            .apply {
                assistantMessages?.let { this.messages(stringToMessageObject(assistantMessages)) }
            }
            .stream()
            .content()
    }


    private fun stringToMessageObject(msgStrings: List<String>): List<Message> {
        return msgStrings.stream()
            .map { content: String? ->
                AssistantMessage(
                    content
                )
            }
            .toList()
    }
}
