package org.minturtle.careersupport.interview.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.minturtle.careersupport.interview.entity.InterviewMessage;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterviewMessageResponse that = (InterviewMessageResponse) o;
        return Objects.equals(id, that.id) && sender == that.sender && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sender, content);
    }
}