package org.minturtle.careersupport.testutils

import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.memory.MemoryBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.minturtle.careersupport.auth.utils.ApiTokenProvider
import org.minturtle.careersupport.auth.utils.JwtTokenProvider
import org.minturtle.careersupport.codereview.utils.GithubObjectFactory
import org.minturtle.careersupport.common.service.ChatService
import org.minturtle.careersupport.user.dto.UserInfoDto.Companion.of
import org.minturtle.careersupport.user.entity.User
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*


@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = [OpenAiAutoConfiguration::class])
@Import(
    MongoDBTestConfig::class
)
abstract class IntegrationTest {
    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    protected lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    protected lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    protected lateinit var apiTokenProvider: ApiTokenProvider

    @MockBean
    protected lateinit var chatService: ChatService

    @MockBean
    protected lateinit var githubObjectFactory: GithubObjectFactory

    protected val DEFAULT_USER_RAW_PASSWORD = "password"

    protected suspend fun createUser(username: String = "username", password: String = DEFAULT_USER_RAW_PASSWORD): User {
        val nickname = "nickname"
        return User(
            nickname,
            username,
            encode(password),
            null
        )
    }

    private suspend fun encode(password: String): String =
        CoroutineScope(Dispatchers.IO).async { passwordEncoder.encode(password) }.await()

    protected fun createJwtToken(user: User): String {
        return jwtTokenProvider.sign(of(user), Date())
    }

    protected fun createExpiredToken(user: User): String {
        return jwtTokenProvider.sign(of(user), Date(0))
    }

}


@TestConfiguration
internal class MongoDBTestConfig {
    @Bean(destroyMethod = "shutdown")
    fun mongoServer(): MongoServer {
        return mongoServer
    }

    companion object {
        private val mongoServer = MongoServer(MemoryBackend())

        init {
            mongoServer.bind("localhost", 27018)
        }
    }
}
