package org.minturtle.careersupport.interview.entity

import org.minturtle.careersupport.common.utils.NanoIdGenerator
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "interview_templates")
class InterviewTemplate(
    @Id
    var id: String = NanoIdGenerator.createNanoId(idSize = 10),
    val userId: String,
    val theme: String,
    val createdAt: Instant = Instant.now()
)