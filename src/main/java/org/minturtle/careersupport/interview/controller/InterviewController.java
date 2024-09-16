package org.minturtle.careersupport.interview.controller;


import org.minturtle.careersupport.interview.dto.CreateInterviewTemplateResponse;
import org.minturtle.careersupport.interview.dto.InterviewProcessRequest;
import org.minturtle.careersupport.interview.entity.InterviewMessage;
import org.minturtle.careersupport.interview.service.InterviewService;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.resolvers.annotations.CurrentUser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
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
        Flux<String> interviewQuestion = interviewService.getInterviewQuestion(templateId);

        Mono<Void> saveToDatabase = interviewQuestion
                .collectList()
                .map(s -> String.join("", s))
                .flatMap(c->interviewService.saveMessage(templateId, InterviewMessage.SenderType.INTERVIEWER, c));

        return interviewQuestion
                .publishOn(Schedulers.boundedElastic())
                .doOnComplete(saveToDatabase::subscribe);
    }

    @PostMapping(value = "/process", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doAIInterview(
            @RequestParam String theme,
            @RequestBody InterviewProcessRequest prev
    ){
        return interviewService.getFollowQuestion(theme, prev.getPreviousQuestion(), prev.getPreviousAnswer());
    }
}
