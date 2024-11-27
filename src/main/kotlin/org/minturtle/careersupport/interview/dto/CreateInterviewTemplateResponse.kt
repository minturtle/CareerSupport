package org.minturtle.careersupport.interview.dto

import org.minturtle.careersupport.interview.entity.InterviewTemplate

data class CreateInterviewTemplateResponse(
    val interviewId: String?,
    val theme: String
){

    companion object{
        fun of(interviewTemplate: InterviewTemplate): CreateInterviewTemplateResponse {
            return CreateInterviewTemplateResponse(
                interviewTemplate.id,
                interviewTemplate.theme
            )
        }
    }
}