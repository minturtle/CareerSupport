package org.minturtle.careersupport.interview.service;


import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.common.aop.Logging;
import org.minturtle.careersupport.common.service.ChatService;
import org.minturtle.careersupport.interview.dto.InterviewMessageResponse;
import org.minturtle.careersupport.interview.dto.InterviewTemplateResponse;
import org.minturtle.careersupport.interview.repository.InterviewTemplateRepository;
import org.minturtle.careersupport.interview.dto.CreateInterviewTemplateResponse;
import org.minturtle.careersupport.interview.entity.InterviewMessage;
import org.minturtle.careersupport.interview.entity.InterviewTemplate;
import org.minturtle.careersupport.interview.repository.InterviewMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Logging
public class InterviewService {

    private final ChatService chatService;

    private final InterviewTemplateRepository interviewTemplateRepository;

    private final InterviewMessageRepository interviewMessageRepository;

    @Value("${spring.ai.openai.messages.interview-system-message}")
    private String interviewSystemMessage;

    @Value("${spring.ai.openai.messages.follow-system-message}")
    private String followSystemMessage;

    public Flux<InterviewTemplateResponse> getTemplatesByUserId(String userId, int page, int size) {
        return interviewTemplateRepository
                .findByUserId(userId, PageRequest.of(page, size))
                .map(InterviewTemplateResponse::of);
    }

    public Flux<InterviewMessageResponse> getMessagesByTemplateIdWithMessageIdCursor(String templateId, String cursor, int size) {
        Pageable pageable = PageRequest.of(0, size);

        Flux<InterviewMessage> messagesFlux;
        if (cursor == null || cursor.isEmpty()) {
            messagesFlux = interviewMessageRepository
                    .findTopNByTemplateIdOrderByIdDesc(templateId, pageable);
        } else {
            messagesFlux = interviewMessageRepository
                    .findByTemplateIdAndIdLessThanEqualOrderByIdDesc(templateId, cursor, pageable);
        }

        return messagesFlux
                .map(InterviewMessageResponse::of);
    }

    public Mono<CreateInterviewTemplateResponse> createTemplate(
            String userId, String theme
    ){
        InterviewTemplate interviewTemplate = InterviewTemplate.builder().userId(userId).theme(theme).build();

        return interviewTemplateRepository.save(interviewTemplate)
            .map(CreateInterviewTemplateResponse::of);
    }

    public Flux<String> getInterviewQuestion(String templateId){
        Mono<InterviewTemplate> templateMono = interviewTemplateRepository.findById(templateId);

        return templateMono.flatMapMany(template ->
                chatService.generate(interviewSystemMessage, template.getTheme())
        );
    }

    public Flux<String> getFollowQuestion(String templateId, String answer){
        return interviewMessageRepository.findFirstByTemplateIdAndSenderOrderByCreatedAtDesc(
                templateId,
                InterviewMessage.SenderType.INTERVIEWER
        )
            .map(InterviewMessage::getContent)
            .flatMapMany(
                previousQuestion->chatService.generate(followSystemMessage, List.of(previousQuestion), answer)
            );
    }

    public Mono<Void> saveMessage(String templateId, InterviewMessage.SenderType sender, String content) {
        InterviewMessage message = InterviewMessage.builder()
                .templateId(templateId)
                .sender(sender)
                .content(content)
                .build();

        return interviewMessageRepository.save(message).then();
    }
}