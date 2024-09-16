package org.minturtle.careersupport.interview.repository;

import org.minturtle.careersupport.interview.entity.InterviewMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface InterviewMessageRepository extends ReactiveMongoRepository<InterviewMessage, String> {
}
