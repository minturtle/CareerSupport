package org.minturtle.careersupport.interview.repository

import kotlinx.coroutines.flow.Flow
import org.minturtle.careersupport.interview.entity.InterviewTemplate
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import reactor.core.publisher.Flux

interface InterviewTemplateRepository: CoroutineCrudRepository<InterviewTemplate, String> {
    suspend fun findByUserId(userId: String, pageable: Pageable): Flow<InterviewTemplate>
}