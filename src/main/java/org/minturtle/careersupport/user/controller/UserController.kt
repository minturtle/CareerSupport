package org.minturtle.careersupport.user.controller

import org.minturtle.careersupport.common.exception.BadRequestException
import org.minturtle.careersupport.common.exception.UnAuthorizedException
import org.minturtle.careersupport.user.dto.*
import org.minturtle.careersupport.user.resolvers.annotations.CurrentUser
import org.minturtle.careersupport.user.service.UserService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/register")
    suspend fun registerUser(@RequestBody registrationDto: UserRegistrationRequest): ResponseEntity<UserInfoDto> {
        val userInfo = userService.registerUser(registrationDto)

        return ResponseEntity.ok(userInfo)
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody loginRequest: UserLoginRequest): ResponseEntity<UserLoginResponse> {
        val loginResult = userService.login(loginRequest.username, loginRequest.password)

        return ResponseEntity.ok(loginResult)
    }

    @GetMapping("/info")
    suspend fun getUserInfoByToken(@RequestHeader(HttpHeaders.AUTHORIZATION) authHeader: String?): ResponseEntity<UserInfoResponse> {
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            throw BadRequestException("Access Token을 찾을 수 없습니다.")
        }
        val token = authHeader.substring(7)

        val userInfo = runCatching {
            userService.getUserInfo(token)
        }.onFailure { e -> throw UnAuthorizedException(e.message) }
            .getOrThrow()

        return ResponseEntity.ok(userInfo)
    }

    @GetMapping("/api-token")
    suspend fun getUserApiToken(
        @CurrentUser user: UserInfoDto
    ): ResponseEntity<UserApiAccessTokenResponse> {
        val apiAccessToken = userService.getUserApiAccessToken(user.id)

        return ResponseEntity.status(201).body(apiAccessToken)

    }
}