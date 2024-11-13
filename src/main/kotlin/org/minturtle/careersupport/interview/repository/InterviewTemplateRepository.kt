package org.minturtle.careersupport.interview.repository

import org.minturtle.careersupport.interview.entity.InterviewTemplate
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface InterviewTemplateRepository: ReactiveMongoRepository<InterviewTemplate, String> {
    fun findByUserId(userId: String?, pageable: Pageable?): Flux<InterviewTemplate>
}