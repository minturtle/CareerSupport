package org.minturtle.careersupport.common.controller

import org.minturtle.careersupport.user.dto.UserInfoDto
import org.minturtle.careersupport.user.resolvers.annotations.CurrentUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api")
class CommonController {
    @GetMapping("/health-check")
    @ResponseStatus(HttpStatus.OK)
    fun healthCheck() = "ok"

    @GetMapping("/api-check")
    @ResponseStatus(HttpStatus.OK)
    fun apiTokenCheck(
        @CurrentUser user: UserInfoDto
    ) = "hello ${user.nickname}"
}