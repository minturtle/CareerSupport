package org.minturtle.careersupport.interview.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class InterviewController {


    @GetMapping
    public Mono<String> test(){
        return Mono.just("Hello");
    }
}
