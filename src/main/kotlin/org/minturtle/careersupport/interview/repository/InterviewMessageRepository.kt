package org.minturtle.careersupport.interview.repository

import kotlinx.coroutines.flow.Flow
import org.minturtle.careersupport.interview.entity.InterviewMessage
import org.minturtle.careersupport.interview.entity.InterviewMessage.SenderType
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


interface InterviewMessageRepository : CoroutineCrudRepository<InterviewMessage, String> {

    suspend fun findFirstByTemplateIdAndSenderOrderByCreatedAtDesc(
        templateId: String,
        sender: SenderType
    ): InterviewMessage?


    // N개의 데이터를 조회, messageId를 커서로 받음
    suspend fun findByTemplateIdAndIdLessThanEqualOrderByIdDesc(
        templateId: String,
        messageId: String,
        pageable: Pageable
    ): Flow<InterviewMessage>

    // 최근 N개의 데이터를 조회
    suspend fun findTopNByTemplateIdOrderByIdDesc(templateId: String, pageable: Pageable): Flow<InterviewMessage>
}