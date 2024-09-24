package org.minturtle.careersupport.interview.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.minturtle.careersupport.interview.entity.InterviewTemplate;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterviewTemplateResponse that = (InterviewTemplateResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(theme, that.theme) &&
                Objects.equals(createdAt.toEpochSecond(), that.createdAt.toEpochSecond());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, theme, createdAt.toEpochSecond());
    }
}