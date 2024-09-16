package org.minturtle.careersupport.interview.repository;

import org.minturtle.careersupport.interview.entity.InterviewMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface InterviewMessageRepository extends ReactiveMongoRepository<InterviewMessage, String> {

    Mono<InterviewMessage> findFirstByTemplateIdAndSenderOrderByCreatedAtDesc(String templateId, InterviewMessage.SenderType sender);

}
