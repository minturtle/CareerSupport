package org.minturtle.careersupport.user.service;


import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.auth.utils.JwtTokenProvider;
import org.minturtle.careersupport.common.exception.ConflictException;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.dto.UserRegistrationRequest;
import org.minturtle.careersupport.user.entity.User;
import org.minturtle.careersupport.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public Mono<UserInfoDto> registerUser(UserRegistrationRequest registrationDto) {
        return userRepository.findByUsername(registrationDto.getUsername())
                .flatMap(existingUser -> Mono.error(new ConflictException("Username already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = User.builder()
                            .nickname(registrationDto.getNickname())
                            .username(registrationDto.getUsername())
                            .password(passwordEncoder.encode(registrationDto.getPassword()))
                            .build();
                    return userRepository.save(user);
                }))
                .map(UserInfoDto::of)
                .cast(UserInfoDto.class);
    }



    public Mono<String> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> jwtTokenProvider.sign(user.getId(), new Date()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")));
    }
}
