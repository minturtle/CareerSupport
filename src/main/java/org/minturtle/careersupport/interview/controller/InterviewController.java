package org.minturtle.careersupport.interview.controller;


import org.minturtle.careersupport.common.dto.CursoredResponse;
import org.minturtle.careersupport.interview.dto.CreateInterviewTemplateResponse;
import org.minturtle.careersupport.interview.dto.InterviewMessageResponse;
import org.minturtle.careersupport.interview.dto.InterviewProcessRequest;
import org.minturtle.careersupport.interview.dto.InterviewTemplateResponse;
import org.minturtle.careersupport.interview.entity.InterviewMessage;
import org.minturtle.careersupport.interview.service.InterviewService;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.resolvers.annotations.CurrentUser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping("/templates")
    public Mono<List<InterviewTemplateResponse>> getTemplatesByUserId(
            @CurrentUser UserInfoDto userInfo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return interviewService
                .getTemplatesByUserId(userInfo.getId(), page, size)
                .collectList();
    }

    @GetMapping("/messages")
    public Mono<CursoredResponse<InterviewMessageResponse>> getMessagesByTemplateId(
            @RequestParam String templateId,
            @RequestParam(required = false) String messageId,
            @RequestParam(defaultValue = "10") int size
    ) {
        return interviewService
                .getMessagesByTemplateIdWithMessageIdCursor(templateId, messageId, size+1)
                .collectList()
                .map(list -> {
                    String nextCursor = null;
                    if (list.size() > size) {
                        nextCursor = list.get(size).getId();
                        list.remove(size);
                    }

                    Collections.reverse(list);
                    return CursoredResponse.<InterviewMessageResponse>builder()
                            .cursor(nextCursor)
                            .data(list)
                            .build();
                });
    }

    @PostMapping("/new")
    public Mono<CreateInterviewTemplateResponse> newInterview(
            @RequestParam(required = true) String theme,
            @CurrentUser UserInfoDto userInfo
    ){
        return interviewService.createTemplate(userInfo.getId(), theme);
    }

    @PostMapping(value = "/start/{templateId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> startAIInterview(
            @PathVariable String templateId
    ){
        Flux<String> interviewQuestion = interviewService.getInterviewQuestion(templateId).cache();

        Mono<Void> saveQuestion = onCompleteSaveMessage(
                interviewQuestion,
                templateId,
                InterviewMessage.SenderType.INTERVIEWER
        );

        return interviewQuestion
                .doOnComplete(saveQuestion::subscribe);
    }

    @PostMapping(value = "/answer/{templateId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doAIInterview(
            @PathVariable String templateId,
            @RequestBody InterviewProcessRequest reqBody
    ){
        Flux<String> followQuestion = interviewService.getFollowQuestion(templateId, reqBody.getAnswer()).cache();

        Mono<Void> saveQuestion = onCompleteSaveMessage(
                followQuestion,
                templateId,
                InterviewMessage.SenderType.INTERVIEWER
        );

        Mono<Void> saveAnswer = onCompleteSaveMessage(
                Flux.just(reqBody.getAnswer()),
                templateId,
                InterviewMessage.SenderType.USER
        );

        return followQuestion
                .doOnComplete(() -> {
                    saveAnswer
                            .then(saveQuestion)
                            .subscribe();
                });
    }

    private Mono<Void> onCompleteSaveMessage(Flux<String> message, String templateId, InterviewMessage.SenderType sender){
        return message
                .collectList()
                .map(s -> String.join("", s))
                .flatMap(c->interviewService.saveMessage(templateId, sender, c));
    }
}