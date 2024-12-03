package org.minturtle.careersupport.codereview.respository

import org.minturtle.careersupport.codereview.entity.CommitPinpoint
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ReviewPinpointRepository : CoroutineCrudRepository<CommitPinpoint, String> {

    suspend fun findByPrNumberAndRepositoryName(prNumber: Int, repositoryName: String): CommitPinpoint?

}