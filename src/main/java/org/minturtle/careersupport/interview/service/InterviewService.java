package org.minturtle.careersupport.interview.service;


import org.minturtle.careersupport.common.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class InterviewService {

    private final ChatService chatService;
    private final String interviewSystemMessage;
    private final String followSystemMessage;
    public InterviewService(
            ChatService chatService,
            @Value("${spring.ai.openai.messages.interview-system-message}") String interviewSystemMessage,
            @Value("${spring.ai.openai.messages.follow-system-message}") String followSystemMessage
        ) {
        this.chatService = chatService;
        this.interviewSystemMessage = interviewSystemMessage;
        this.followSystemMessage = followSystemMessage;
    }

    public Flux<String> getInterviewQuestion(String theme){
        return chatService.generate(interviewSystemMessage, theme);
    }

    public Flux<String> getFollowQuestion(String theme, String previousQuestion, String previousAnswer){
        return chatService.generate(followSystemMessage, List.of(previousQuestion, previousAnswer), theme);
    }
}
