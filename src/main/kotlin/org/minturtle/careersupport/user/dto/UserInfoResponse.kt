package org.minturtle.careersupport.user.dto

import org.minturtle.careersupport.user.entity.User

data class UserInfoResponse(
    val nickname: String
){

    companion object{
        @JvmStatic
        fun of(user: User): UserInfoResponse {
            return UserInfoResponse(user.nickname)
        }

        @JvmStatic
        fun of(user: UserInfoDto): UserInfoResponse {
            return UserInfoResponse(user.nickname)
        }
    }

}