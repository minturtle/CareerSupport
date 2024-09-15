package org.minturtle.careersupport.interview.service;


import org.minturtle.careersupport.common.service.ChatService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class InterviewService {

    private final ChatService chatService;

    public InterviewService(ChatService chatService) {
        this.chatService = chatService;
    }



    public Flux<String> getInterviewQuestion(String theme){
        String systemMessage = """
        당신은 AI 면접관입니다. 역할은 다음과 같습니다:
        1. 사용자가 입력한 주제에 맞는 면접 질문을 하나씩 생성합니다.
        2. 사용자가 질문에 답변을 하면, 그 답변을 평가하고 점수를 부여합니다.
        3. 답변에 대한 피드백을 제공하며, 추가적인 꼬리 질문을 제시합니다.
        4. 모든 질문과 답변은 한글로 이루어 집니다.
        5. 다른 부가 설명 없이, 오직 질문또는 평가만을 해야합니다.
        """;

        return chatService.generate(systemMessage, theme);
    }

    public Flux<String> getFollowQuestion(String theme, String previousQuestion, String previousAnswer){
        String systemMessage = """
        당신은 AI 면접관입니다. 역할은 다음과 같습니다:
        1. 사용자가 입력한 주제에 맞는 면접 질문을 하나씩 생성합니다.
        2. 사용자가 질문에 답변을 하면, 그 답변을 평가하고 점수를 부여합니다.
        3. 답변에 대한 피드백을 제공하며, 추가적인 꼬리 질문을 제시합니다.
        4. 모든 질문과 답변은 한글로 이루어 집니다.
        5. 다른 부가 설명 없이, 오직 질문또는 평가만을 해야합니다.
        """;

        return chatService.generate(systemMessage, List.of(previousQuestion, previousAnswer), theme);
    }
}
