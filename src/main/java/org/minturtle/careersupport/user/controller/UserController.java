package org.minturtle.careersupport.user.controller;


import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.common.exception.ConflictException;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.dto.UserRegistrationDto;
import org.minturtle.careersupport.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @PostMapping("/register")
    public Mono<ResponseEntity<UserInfoDto>> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        return userService.registerUser(registrationDto)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof ConflictException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}