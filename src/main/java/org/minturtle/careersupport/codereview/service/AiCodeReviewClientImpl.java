package org.minturtle.careersupport.codereview.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.codereview.dto.PullRequestFile;
import org.minturtle.careersupport.common.exception.InternalServerException;
import org.minturtle.careersupport.common.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AiCodeReviewClientImpl implements AiCodeReviewClient{

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.messages.code-review-system-message}")
    private String codeReviewSystemMessage;

    @Override
    public Flux<ReviewResponse> getAiCodeReview(Flux<ReviewRequest> files) {
        return files.flatMap(
                file-> {
                    try {
                        return chatService.generate(codeReviewSystemMessage, objectMapper.writeValueAsString(file))
                                .collect(Collectors.joining())
                                .map(result -> new ReviewResponse(file.getFileName(), result));
                    } catch (JsonProcessingException e) {
                        throw new InternalServerException("답변 생성 중 오류가 발생했습니다.");
                    }
                }
        );
    }
}