package org.minturtle.careersupport.user.repository

import org.minturtle.careersupport.user.entity.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface UserRepository : ReactiveMongoRepository<User, String> {
    suspend fun findByUsername(username : String) : User?
}