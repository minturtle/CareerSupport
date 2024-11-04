package org.minturtle.careersupport.user.dto

import org.minturtle.careersupport.user.entity.User

data class UserInfoDto(
     val id: String,
     val nickname: String,
     val username: String
) {

    companion object{

        @JvmStatic
        fun of(user: Any): UserInfoDto {
            if (user is User) {
                return UserInfoDto(
                    user.id,
                    user.nickname,
                    user.username
                )
            }
            throw IllegalStateException("Unexpected object type: " + user.javaClass.getName())
        }

    }
}