package org.minturtle.careersupport.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.minturtle.careersupport.testutils.IntegrationTest;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.dto.UserLoginRequest;
import org.minturtle.careersupport.user.dto.UserLoginResponse;
import org.minturtle.careersupport.user.dto.UserRegistrationRequest;
import org.minturtle.careersupport.user.entity.User;
import org.minturtle.careersupport.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
class UserControllerTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("사용자는 아직 회원가입 되지 않은 Username과 Password로 회원가입을 할 수 있다.")
    void registerUserSuccess() {
        // given
        String nickname = "nickname";
        String username = "username";
        String password = "password";
        UserRegistrationRequest request = new UserRegistrationRequest(nickname, username, password);

        // when
        UserInfoDto actual = webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserInfoDto.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getUsername()).isEqualTo(username);
        assertThat(actual.getNickname()).isEqualTo(nickname);

    }


    @Test
    @DisplayName("사용자는 이미 회원가입 된 username을 입력할 시 409 코드를 반환받는다.")
    void registerUserDuplication() {
        //given
        String nickname = "nickname";
        String username = "username";
        String password = "password";


        userRepository.save(new User("123", "nick", username, password)).block();
        UserRegistrationRequest request = new UserRegistrationRequest(nickname, username, password);

        //when & then
        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("회원가입이 완료된 사용자는 로그인을 수행해 JWT 토큰을 반환받을 수 있다.")
    public void loginUserSuccess() throws Exception{
        //given
        String nickname = "nickname";
        String username = "username";
        String password = "password";

        userRepository.save(new User("123", nickname, username, passwordEncoder.encode(password))).block();

        //when
        UserLoginRequest request = new UserLoginRequest(username, password);
        UserLoginResponse actual = webTestClient.post()
                .uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserLoginResponse.class)
                .returnResult()
                .getResponseBody();
        //then
        assertThat(actual.getNickname()).isEqualTo(nickname);
        assertThat(actual.getToken()).isNotNull();
    }


    @ParameterizedTest
    @MethodSource("getLoginTestArguments")
    @DisplayName("잘못된 Username 또는 Password를 입력할 시 401 코드를 반환받는다")
    public void loginInvalidUsername(
            String correctUsername,
            String correntPassword,
            String inputUsername,
            String inputPassword
    ) throws Exception{
        //given
        userRepository.save(new User("123", "nickname", correctUsername, passwordEncoder.encode(correntPassword))).block();

        //when & then
        UserLoginRequest request = new UserLoginRequest(inputUsername, inputPassword);
        webTestClient.post()
                .uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();

    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
    }


    protected static Stream<Arguments> getLoginTestArguments(){
        return Stream.of(
                Arguments.of("username", "password", "user", "password"),
                Arguments.of("username", "password", "username", "pwd"),
                Arguments.of("username", "password", "user", "pwd")
        );
    }
}