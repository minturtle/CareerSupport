package org.minturtle.careersupport.interview.dto

import org.minturtle.careersupport.interview.entity.InterviewMessage
import org.minturtle.careersupport.interview.entity.InterviewMessage.SenderType

data class InterviewMessageResponse(
    val id: String,
    val sender: SenderType,
    val content: String
){


    companion object{
        fun of(interviewMessage: InterviewMessage): InterviewMessageResponse {
            return InterviewMessageResponse(
                id = interviewMessage.id,
                sender = interviewMessage.sender,
                content = interviewMessage.content
            )        }


    }


}