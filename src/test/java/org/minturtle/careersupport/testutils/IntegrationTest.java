package org.minturtle.careersupport.testutils;



import org.minturtle.careersupport.auth.utils.JwtTokenProvider;
import org.minturtle.careersupport.common.service.ChatService;
import org.minturtle.careersupport.user.dto.UserInfoDto;
import org.minturtle.careersupport.user.entity.User;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = OpenAiAutoConfiguration.class)
public abstract class IntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @MockBean
    protected ChatService chatService;


    protected String createJwtToken(User user){
        return jwtTokenProvider.sign(UserInfoDto.of(user), new Date());
    }

}