package org.minturtle.careersupport.interview.repository

import org.minturtle.careersupport.common.aop.Logging
import org.minturtle.careersupport.interview.entity.InterviewMessage
import org.minturtle.careersupport.interview.entity.InterviewMessage.SenderType
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Logging
interface InterviewMessageRepository : ReactiveMongoRepository<InterviewMessage, String> {

    fun findFirstByTemplateIdAndSenderOrderByCreatedAtDesc(
        templateId: String,
        sender: SenderType
    ): Mono<InterviewMessage?>


    // N개의 데이터를 조회, messageId를 커서로 받음
    fun findByTemplateIdAndIdLessThanEqualOrderByIdDesc(
        templateId: String,
        messageId: String,
        pageable: Pageable
    ): Flux<InterviewMessage>

    // 최근 N개의 데이터를 조회
    fun findTopNByTemplateIdOrderByIdDesc(templateId: String, pageable: Pageable): Flux<InterviewMessage>
}