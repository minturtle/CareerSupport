package org.minturtle.careersupport.user.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.minturtle.careersupport.auth.utils.ApiTokenProvider
import org.minturtle.careersupport.auth.utils.JwtTokenProvider
import org.minturtle.careersupport.common.exception.ConflictException
import org.minturtle.careersupport.common.exception.UnAuthorizedException
import org.minturtle.careersupport.user.dto.*
import org.minturtle.careersupport.user.entity.User
import org.minturtle.careersupport.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val apiTokenProvider: ApiTokenProvider
){

    suspend fun registerUser(registrationDto: UserRegistrationRequest): UserInfoDto {
        userRepository.findByUsername(registrationDto.username)?.let {
            throw ConflictException("Username already exists")
        }

        val encodedPassword = passwordEncoder.encode(registrationDto.password)

        val newUser = User(
            registrationDto.nickname,
            registrationDto.username,
            encodedPassword
        )

        val savedUser = userRepository.save(newUser)

        return UserInfoDto.of(savedUser)
    }

    suspend fun login(username: String, password: String): UserLoginResponse {
        val user = userRepository.findByUsername(username)
            ?.takeIf { passwordEncoder.matchesAsync(password, it.password) }
            ?: throw UnAuthorizedException("Invalid credentials")

        val jwt = CoroutineScope(Dispatchers.IO).async {
            jwtTokenProvider.sign(UserInfoDto.of(user), Date())
        }.await()

        return UserLoginResponse.of(user, jwt)
    }

    suspend fun getUserInfo(token: String): UserInfoResponse {
        val verifiedUserInfo = CoroutineScope(Dispatchers.IO).async {
            jwtTokenProvider.verify(token)
        }.await()

        return UserInfoResponse.of(verifiedUserInfo)
    }

    suspend fun getUserApiAccessToken(id: String): UserApiAccessTokenResponse {
        val findUser = userRepository.findById(id) ?: UnAuthorizedException("사용자 정보를 조회할 수 없습니다.") as User

        val token = CoroutineScope(Dispatchers.IO).async {
            apiTokenProvider.generate(UserInfoDto.of(findUser))
        }.await()

        findUser.apiToken = token

        userRepository.save(findUser)
        return UserApiAccessTokenResponse(token)
    }

    private suspend fun PasswordEncoder.matchesAsync(rawPassword: String, encodedPassword: String) : Boolean{
        return CoroutineScope(Dispatchers.IO).async {
            passwordEncoder.matches(rawPassword, encodedPassword)
        }.await()
    }
}