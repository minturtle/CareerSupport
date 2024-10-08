package org.minturtle.careersupport.interview.repository;

import org.minturtle.careersupport.common.aop.Logging;
import org.minturtle.careersupport.interview.entity.InterviewMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;


@Logging
public interface InterviewMessageRepository extends ReactiveMongoRepository<InterviewMessage, String> {

    Mono<InterviewMessage> findFirstByTemplateIdAndSenderOrderByCreatedAtDesc(String templateId, InterviewMessage.SenderType sender);

    Flux<InterviewMessage> findByTemplateIdAndIdLessThanEqualOrderByIdDesc(String templateId, String messageId, Pageable pageable);
    Flux<InterviewMessage> findTopNByTemplateIdOrderByIdDesc(String templateId, Pageable pageable);
}