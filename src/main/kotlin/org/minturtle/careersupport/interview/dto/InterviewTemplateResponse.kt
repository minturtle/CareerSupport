package org.minturtle.careersupport.interview.dto

import org.minturtle.careersupport.interview.entity.InterviewTemplate
import java.time.ZoneId
import java.time.ZonedDateTime

data class InterviewTemplateResponse(
    val id: String,
    val theme: String,
    val createdAt: ZonedDateTime
    ) {


    companion object{
        fun of(interviewTemplate: InterviewTemplate): InterviewTemplateResponse {
            return InterviewTemplateResponse(
                id = interviewTemplate.id,
                theme = interviewTemplate.theme,
                createdAt = ZonedDateTime.ofInstant(interviewTemplate.createdAt, ZoneId.systemDefault())
            )
        }
    }
}


