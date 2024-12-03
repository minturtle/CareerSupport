package org.minturtle.careersupport.user.repository

import org.minturtle.careersupport.user.entity.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, String> {

    suspend fun findByUsername(username : String) : User?
}