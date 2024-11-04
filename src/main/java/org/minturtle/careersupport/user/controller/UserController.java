package org.minturtle.careersupport.user.controller;


import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.common.exception.BadRequestException;
import org.minturtle.careersupport.common.exception.UnAuthorizedException;
import org.minturtle.careersupport.user.dto.*;
import org.minturtle.careersupport.user.resolvers.annotations.CurrentUser;
import org.minturtle.careersupport.user.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public Mono<ResponseEntity<UserInfoDto>> registerUser(@RequestBody UserRegistrationRequest registrationDto) {
        return userService.registerUser(registrationDto)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<UserLoginResponse>> login(@RequestBody UserLoginRequest loginRequest) {
        return userService.login(loginRequest.getUsername(), loginRequest.getPassword())
                .map(ResponseEntity::ok);
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<UserInfoResponse>> getUserInfoByToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader){
        if(authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")){
            throw new BadRequestException("Access Token을 찾을 수 없습니다.");
        }

        String token = authHeader.substring(7);

        return userService.getUserInfo(token)
                .onErrorResume(e->{ throw new UnAuthorizedException(e.getMessage());})
                .map(ResponseEntity::ok);
    }

    @GetMapping("/api-token")
    public Mono<ResponseEntity<UserApiAccessTokenResponse>> getUserApiToken(
            @CurrentUser UserInfoDto user
    ){
        return userService.getUserApiAccessToken(user.getId()).map(
                token -> ResponseEntity.status(HttpStatusCode.valueOf(201)).body(token)
        );
    }
}