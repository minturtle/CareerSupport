package org.minturtle.careersupport.interview.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.minturtle.careersupport.interview.entity.InterviewMessage;

@Getter
@AllArgsConstructor
@Builder
public class InterviewMessageResponse {

    private String id;
    private InterviewMessage.SenderType sender;
    private String content;

    public static InterviewMessageResponse of(InterviewMessage interviewMessage){
        return InterviewMessageResponse.builder()
                .id(interviewMessage.getId())
                .sender(interviewMessage.getSender())
                .content(interviewMessage.getContent())
                .build();
    }
}
