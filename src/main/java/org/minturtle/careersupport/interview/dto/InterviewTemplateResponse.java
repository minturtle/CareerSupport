package org.minturtle.careersupport.interview.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.minturtle.careersupport.interview.entity.InterviewTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
@Builder
public class InterviewTemplateResponse {

    private String id;
    private String theme;
    private ZonedDateTime createdAt;

    public static InterviewTemplateResponse of(InterviewTemplate interviewTemplate){
        return InterviewTemplateResponse.builder()
                .id(interviewTemplate.getId())
                .theme(interviewTemplate.getTheme())
                .createdAt(ZonedDateTime.ofInstant(interviewTemplate.getCreatedAt(), ZoneId.systemDefault()))
                .build();
    }

}
