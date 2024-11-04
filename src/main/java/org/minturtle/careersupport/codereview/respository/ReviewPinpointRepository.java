package org.minturtle.careersupport.codereview.respository;

import org.minturtle.careersupport.codereview.entity.CommitPinpoint;
import org.minturtle.careersupport.common.aop.Logging;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

@Logging
public interface ReviewPinpointRepository extends ReactiveMongoRepository<CommitPinpoint, String> {
    Mono<CommitPinpoint> findByPrNumber(Integer prNumber);
}
