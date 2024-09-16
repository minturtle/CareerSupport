package org.minturtle.careersupport.interview.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class CreateInterviewTemplateResponse {

    private String interviewId;
    private String theme;
}
