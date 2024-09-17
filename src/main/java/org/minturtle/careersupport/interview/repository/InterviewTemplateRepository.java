package org.minturtle.careersupport.interview.repository;

import org.minturtle.careersupport.interview.entity.InterviewTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface InterviewTemplateRepository extends ReactiveMongoRepository<InterviewTemplate, String> {
    Flux<InterviewTemplate> findByUserId(String userId, Pageable pageable);
}


