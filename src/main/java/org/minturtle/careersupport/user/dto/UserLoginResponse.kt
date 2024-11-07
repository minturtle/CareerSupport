package org.minturtle.careersupport.user.dto

import org.minturtle.careersupport.user.entity.User

data class UserLoginResponse(

    val nickname: String,
    val token: String

){

    companion object{
        @JvmStatic
        fun of(user : User, token: String): UserLoginResponse {
            return UserLoginResponse(
                user.nickname,
                token
            )
        }
    }

}
