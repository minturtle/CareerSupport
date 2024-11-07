package org.minturtle.careersupport.testutils;



import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.minturtle.careersupport.auth.utils.ApiTokenProvider;
import org.minturtle.careersupport.auth.utils.JwtTokenProvider;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.entity.User;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = OpenAiAutoConfiguration.class)
@Import(MongoDBTestConfig.class)
public abstract class IntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected ApiTokenProvider apiTokenProvider;

    @MockBean
    protected ChatService chatService;

    @MockBean
    protected GithubUtils githubUtils;

    protected static final String DEFAULT_USER_RAW_PASSWORD = "password";
    protected User createUser(){
        return createUser("username", DEFAULT_USER_RAW_PASSWORD);
    }

    protected User createUser(String username, String password){
        String nickname = "nickname";

        return new User(nickname, username, passwordEncoder.encode(password), null);

    }

    protected String createJwtToken(User user){
        return jwtTokenProvider.sign(UserInfoDto.of(user), new Date()).block();
    }

    protected String createExpiredToken(User user){
        return jwtTokenProvider.sign(UserInfoDto.of(user), new Date(0)).block();
    }

}


@TestConfiguration
class MongoDBTestConfig{

    private static MongoServer mongoServer = new MongoServer(new MemoryBackend());

    static {
        mongoServer.bind("localhost", 27018);
    }

    @Bean(destroyMethod = "shutdown")
    public MongoServer mongoServer(){
        return mongoServer;
    }

}