package org.minturtle.careersupport.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Document(collection = "interview_messages")
public class InterviewMessage {

    @Id
    private String id;

    private String templateId;

    private SenderType sender;

    private String content;

    @Builder.Default
    private Instant createdAt = Instant.now();

    public enum SenderType{
        USER, INTERVIEWER
    }
}
