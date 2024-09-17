package org.minturtle.careersupport.interview.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.ZonedDateTime;

@Document(collection = "interview_templates")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class InterviewTemplate {

    @Id
    private String id;
    private String userId;
    private String theme;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
