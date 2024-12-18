package org.minturtle.careersupport.user.service;


import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.auth.utils.ApiTokenProvider;
import org.minturtle.careersupport.auth.utils.JwtTokenProvider;
import org.minturtle.careersupport.common.aop.Logging;
import org.minturtle.careersupport.common.exception.ConflictException;
import org.minturtle.careersupport.common.exception.UnAuthorizedException;
import org.minturtle.careersupport.user.dto.*;
import org.minturtle.careersupport.user.entity.User;
import org.minturtle.careersupport.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Logging
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApiTokenProvider apiTokenProvider;


    public Mono<UserInfoDto> registerUser(UserRegistrationRequest registrationDto) {
        return userRepository.findByUsername(registrationDto.getUsername())
                .flatMap(existingUser -> Mono.error(new ConflictException("Username already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    return Mono.fromCallable(() -> passwordEncoder.encode(registrationDto.getPassword()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(encodedPassword -> {
                                User user = User.builder()
                                        .nickname(registrationDto.getNickname())
                                        .username(registrationDto.getUsername())
                                        .password(encodedPassword)
                                        .build();
                                return userRepository.save(user);
                            });
                }))
                .map(UserInfoDto::of)
                .cast(UserInfoDto.class);
    }



    public Mono<UserLoginResponse> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .flatMap(user -> jwtTokenProvider.sign(UserInfoDto.of(user), new Date())
                        .map(token -> createUserLoginResponse(user, token)))
                .switchIfEmpty(Mono.error(new UnAuthorizedException("Invalid credentials")));
    }

    public Mono<UserInfoResponse> getUserInfo(String token){
        return jwtTokenProvider.verify(token)
                .map(UserInfoResponse::of);
    }

    private UserLoginResponse createUserLoginResponse(User user, String jwt){
        return new UserLoginResponse(user.getNickname(), jwt);
    }

    public Mono<UserApiAccessTokenResponse> getUserApiAccessToken(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UnAuthorizedException("사용자를 조회할 수 없습니다.")))
                .flatMap(user -> apiTokenProvider.generate(UserInfoDto.of(user))
                        .flatMap(token -> {
                            user.setApiToken(token);
                            return userRepository.save(user).thenReturn(token);
                        })
                )
                .map(UserApiAccessTokenResponse::new);
    }
}