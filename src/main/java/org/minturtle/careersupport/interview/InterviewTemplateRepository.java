package org.minturtle.careersupport.interview;

import org.minturtle.careersupport.interview.entity.InterviewTemplate;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface InterviewTemplateRepository extends ReactiveMongoRepository<InterviewTemplate, String> {
}
