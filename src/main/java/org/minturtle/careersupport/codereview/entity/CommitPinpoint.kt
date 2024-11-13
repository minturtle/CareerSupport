package org.minturtle.careersupport.codereview.entity

import org.minturtle.careersupport.common.utils.NanoIdGenerator
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant


@Document(collection = "commit_pinpoint")
class CommitPinpoint(
    @Id
    val id: String = NanoIdGenerator.createNanoId(idSize = 10),
    var lastSha: String,
    val prNumber: Int,
    val repositoryName: String,
    val createdAt: Instant = Instant.now()
)