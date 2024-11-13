package org.minturtle.careersupport.interview.entity

import org.minturtle.careersupport.common.utils.NanoIdGenerator
import org.springframework.data.annotation.Id
import java.time.Instant

class InterviewMessage(
    @Id
    var id: String = NanoIdGenerator.createNanoId(idSize = 10),
    val templateId: String,
    val sender: SenderType,
    val content: String,
    val createdAt: Instant = Instant.now()

) {

    enum class SenderType {
        USER,
        INTERVIEWER
    }

}