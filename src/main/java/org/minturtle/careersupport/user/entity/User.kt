package org.minturtle.careersupport.user.entity

import org.minturtle.careersupport.common.entity.BaseEntity
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
class User(
    var nickname: String,

    @Indexed(unique = true)
    val username: String,
    val password: String,
    var apiToken: String? = null
): BaseEntity()