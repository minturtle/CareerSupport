package org.minturtle.careersupport.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.minturtle.careersupport.testutils.IntegrationTest;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.dto.UserRegistrationRequest;
import org.minturtle.careersupport.user.entity.User;
import org.minturtle.careersupport.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.*;
class UserControllerTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자는 아직 회원가입 되지 않은 Username과 Password로 회원가입을 할 수 있다.")
    void registerUser_Success() {
        String nickname = "nickname";
        String username = "username";
        String password = "password";
        UserRegistrationRequest request = new UserRegistrationRequest(nickname, username, password);


        UserInfoDto actual = webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserInfoDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getUsername()).isEqualTo(username);
        assertThat(actual.getNickname()).isEqualTo(nickname);

    }


    @Test
    @DisplayName("사용자는 이미 회원가입 된 username을 입력할 시 409 코드를 반환한다.")
    void registerUser_Duplication() {
        String nickname = "nickname";
        String username = "username";
        String password = "password";


        userRepository.save(new User("123", "nick", username, password)).block();
        UserRegistrationRequest request = new UserRegistrationRequest(nickname, username, password);

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
    }
}