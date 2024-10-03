package org.minturtle.careersupport.user.repository;


import org.minturtle.careersupport.common.aop.Logging;
import org.minturtle.careersupport.user.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

@Logging
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByUsername(String username);
}