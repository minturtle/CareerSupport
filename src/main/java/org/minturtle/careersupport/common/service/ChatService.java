package org.minturtle.careersupport.common.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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

}
