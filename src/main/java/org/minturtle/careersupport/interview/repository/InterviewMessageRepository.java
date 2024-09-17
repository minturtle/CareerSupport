package org.minturtle.careersupport.interview.repository;

import org.minturtle.careersupport.interview.entity.InterviewMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface InterviewMessageRepository extends ReactiveMongoRepository<InterviewMessage, String> {

    Mono<InterviewMessage> findFirstByTemplateIdAndSenderOrderByCreatedAtDesc(String templateId, InterviewMessage.SenderType sender);

    Flux<InterviewMessage> findByTemplateIdAndIdLessThanOrderByIdDesc(String templateId, String messageId, Pageable pageable);

    Flux<InterviewMessage> findByTemplateIdOrderByIdDesc(String templateId, Pageable pageable);
}
