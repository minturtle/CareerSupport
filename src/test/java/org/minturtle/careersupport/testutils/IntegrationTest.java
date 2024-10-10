package org.minturtle.careersupport.testutils;



import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.minturtle.careersupport.auth.utils.JwtTokenProvider;
import org.minturtle.careersupport.codereview.service.CodeReviewService;
import org.minturtle.careersupport.common.service.ChatService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
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
    protected JwtTokenProvider jwtTokenProvider;

    @MockBean
    protected ChatService chatService;

    // TODO : 추후 실제 빈으로 변경
    @MockBean
    protected CodeReviewService codeReviewService;

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