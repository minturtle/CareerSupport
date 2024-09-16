package org.minturtle.careersupport.interview.service;


import org.minturtle.careersupport.common.service.ChatService;
import org.minturtle.careersupport.interview.InterviewTemplateRepository;
import org.minturtle.careersupport.interview.dto.CreateInterviewTemplateResponse;
import org.minturtle.careersupport.interview.entity.InterviewTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class InterviewService {

    private final ChatService chatService;

    private final InterviewTemplateRepository interviewTemplateRepository;
    private final String interviewSystemMessage;
    private final String followSystemMessage;


    public InterviewService(
            ChatService chatService,
            InterviewTemplateRepository interviewTemplateRepository,
            @Value("${spring.ai.openai.messages.interview-system-message}") String interviewSystemMessage,
            @Value("${spring.ai.openai.messages.follow-system-message}") String followSystemMessage
        ) {
        this.chatService = chatService;
        this.interviewTemplateRepository = interviewTemplateRepository;
        this.interviewSystemMessage = interviewSystemMessage;
        this.followSystemMessage = followSystemMessage;
    }

    public Mono<CreateInterviewTemplateResponse> createTemplate(
            String userId, String theme
    ){
        InterviewTemplate interviewTemplate = InterviewTemplate.builder().userId(userId).theme(theme).build();

        return interviewTemplateRepository.save(interviewTemplate)
            .map(CreateInterviewTemplateResponse::of);
    }

    public Flux<String> getInterviewQuestion(String theme){
        return chatService.generate(interviewSystemMessage, theme);
    }

    public Flux<String> getFollowQuestion(String theme, String previousQuestion, String previousAnswer){
        return chatService.generate(followSystemMessage, List.of(previousQuestion, previousAnswer), theme);
    }
}
