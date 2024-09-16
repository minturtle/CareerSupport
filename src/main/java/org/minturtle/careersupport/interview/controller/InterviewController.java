package org.minturtle.careersupport.interview.controller;


import org.minturtle.careersupport.interview.dto.CreateInterviewTemplateResponse;
import org.minturtle.careersupport.interview.dto.InterviewProcessRequest;
import org.minturtle.careersupport.interview.service.InterviewService;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.resolvers.annotations.CurrentUser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @PostMapping(value = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> startAIInterview(
            @RequestParam String theme
    ){
        return interviewService.getInterviewQuestion(theme);
    }

    @PostMapping(value = "/process", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doAIInterview(
            @RequestParam String theme,
            @RequestBody InterviewProcessRequest prev
    ){
        return interviewService.getFollowQuestion(theme, prev.getPreviousQuestion(), prev.getPreviousAnswer());
    }
}
