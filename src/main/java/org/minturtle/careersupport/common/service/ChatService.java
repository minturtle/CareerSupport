package org.minturtle.careersupport.common.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class ChatService {

    private final ChatClient chatClient;
    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    public Flux<String> generate(String systemMessage, String message){
        return chatClient.prompt()
                .system(systemMessage)
                .user(message)
                .stream()
                .content();
    }

    public Flux<String> generate(String systemMessage, List<String> assistantMessages, String message){
        return chatClient.prompt()
                .system(systemMessage)
                .user(message)
                .messages(stringToMessageObject(assistantMessages))
                .stream()
                .content();
    }



    private List<Message> stringToMessageObject(List<String> msgStrings ){
        return  msgStrings.stream()
                .map(AssistantMessage::new)
                .map(message -> (Message) message).toList();
    }
}
