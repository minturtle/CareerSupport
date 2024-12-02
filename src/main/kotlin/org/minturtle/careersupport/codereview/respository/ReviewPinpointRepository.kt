package org.minturtle.careersupport.codereview.respository

import org.minturtle.careersupport.codereview.entity.CommitPinpoint
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface ReviewPinpointRepository : ReactiveMongoRepository<CommitPinpoint, String> {

    fun findByPrNumberAndRepositoryName(prNumber: Int?, repositoryName: String?): Mono<CommitPinpoint?>

}