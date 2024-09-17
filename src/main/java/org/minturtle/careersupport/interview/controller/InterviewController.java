package org.minturtle.careersupport.interview.controller;


import org.minturtle.careersupport.interview.dto.InterviewProcessRequest;
import org.minturtle.careersupport.interview.service.InterviewService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/start")
    public Flux<String> startAIInterview(
            @RequestParam String theme
    ){
        return interviewService.getInterviewQuestion(theme);
    }

    @PostMapping("/process")
    public Flux<String> doAIInterview(
            @RequestParam String theme,
            @RequestBody InterviewProcessRequest prev
    ){
        return interviewService.getFollowQuestion(theme, prev.getPreviousQuestion(), prev.getPreviousAnswer());
    }
}
