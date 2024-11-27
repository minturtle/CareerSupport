package org.minturtle.careersupport.user.controller

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.minturtle.careersupport.testutils.IntegrationTest
import org.minturtle.careersupport.user.dto.*
import org.minturtle.careersupport.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import reactor.test.StepVerifier
import org.assertj.core.api.Assertions.assertThat
import java.util.stream.Stream

class UserControllerTest : IntegrationTest() {

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll().block()
    }

    @Test
    fun `사용자는 아직 회원가입 되지 않은 Username과 Password로 회원가입을 할 수 있다`() {
        // given
        val nickname = "nickname"
        val username = "username"
        val password = "password"
        val request = UserRegistrationRequest(nickname, username, password)

        // when
        val actual = webTestClient.post()
            .uri("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(UserInfoDto::class.java)
            .returnResult()
            .responseBody

        // then
        assertThat(actual?.id).isNotNull()
        assertThat(actual?.username).isEqualTo(username)
        assertThat(actual?.nickname).isEqualTo(nickname)
    }

    @Test
    fun `사용자는 이미 회원가입 된 username을 입력할 시 409 코드를 반환받는다`() = runTest{
        // given
        val user = createUser()
        userRepository.save(user).awaitSingle()

        val request = UserRegistrationRequest(
            user.nickname,
            user.username,
            DEFAULT_USER_RAW_PASSWORD
        )

        // when & then
        webTestClient.post()
            .uri("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `회원가입이 완료된 사용자는 로그인을 수행해 JWT 토큰을 반환받을 수 있다`()= runTest{
        // given
        val user = createUser()
        userRepository.save(user).awaitSingle()

        // when
        val request = UserLoginRequest(user.username, DEFAULT_USER_RAW_PASSWORD)
        val actual = webTestClient.post()
            .uri("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody(UserLoginResponse::class.java)
            .returnResult()
            .responseBody

        // then
        assertThat(actual?.nickname).isEqualTo(user.nickname)
        assertThat(actual?.token).isNotNull()
    }

    @ParameterizedTest
    @MethodSource("getLoginTestArguments")
    fun `잘못된 Username 또는 Password를 입력할 시 401 코드를 반환받는다`(
        correctUsername: String,
        correctPassword: String,
        inputUsername: String,
        inputPassword: String
    ) = runTest{
        // given
        val user = createUser(correctUsername, correctPassword)
        userRepository.save(user).awaitSingle()

        // when & then
        val request = UserLoginRequest(inputUsername, inputPassword)
        webTestClient.post()
            .uri("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `JWT 토큰을 가진 사용자는 자신의 토큰으로 자신의 정보를 조회할 수 있다`() = runTest{
        // given
        val user = createUser()
        userRepository.save(user).awaitSingle()

        // when
        val jwtToken = createJwtToken(user)
        val actual = webTestClient.get()
            .uri("/api/users/info")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserInfoResponse::class.java)
            .returnResult()
            .responseBody!!

        // then
        assertThat(actual.nickname).isEqualTo(user.nickname)
    }

    @Test
    fun `만료된 JWT를 전달한 사용자는 401 오류를 throw한다`() = runTest {
        // given
        val user = createUser()
        userRepository.save(user).awaitSingle()

        // when & then
        val expiredToken = createExpiredToken(user)

        webTestClient.get()
            .uri("/api/users/info")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $expiredToken")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `사용자는 API AccessToken을 발급 받을 수 있다`() = runTest{
        // given
        val user = createUser()
        userRepository.save(user).awaitSingle()

        // when
        val jwtToken = createJwtToken(user)
        val responseBody = webTestClient.get()
            .uri("/api/users/api-token")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .exchange()
            .expectStatus().isCreated
            .expectBody(UserApiAccessTokenResponse::class.java)
            .returnResult()
            .responseBody!!

        // then
        StepVerifier.create(userRepository.findById(user.id))
            .assertNext { savedUser ->
                assertThat(savedUser.apiToken).isEqualTo(responseBody.token)
            }
            .verifyComplete()


        val decryptedUserInfo = apiTokenProvider.decryptApiToken(responseBody.token)

        assertThat(decryptedUserInfo).isEqualTo(UserInfoDto.of(user))
    }

    companion object {
        @JvmStatic
        fun getLoginTestArguments(): Stream<Arguments> = Stream.of(
            Arguments.of("username", "password", "user", "password"),
            Arguments.of("username", "password", "username", "pwd"),
            Arguments.of("username", "password", "user", "pwd")
        )
    }
}