package org.minturtle.careersupport.user.service;


import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.common.exception.ConflictException;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.dto.UserRegistrationDto;
import org.minturtle.careersupport.user.entity.User;
import org.minturtle.careersupport.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public Mono<UserInfoDto> registerUser(UserRegistrationDto registrationDto) {
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
}
