package org.minturtle.careersupport.interview.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.minturtle.careersupport.interview.entity.InterviewTemplate;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class CreateInterviewTemplateResponse {

    private String interviewId;
    private String theme;

    public static CreateInterviewTemplateResponse of(InterviewTemplate interviewTemplate){
        return CreateInterviewTemplateResponse.builder()
                .interviewId(interviewTemplate.getId())
                .theme(interviewTemplate.getTheme())
                .build();
    }
}
