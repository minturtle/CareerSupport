package org.minturtle.careersupport.testutils;


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClients;
import de.flapdoodle.embed.mongo.transitions.MongodStarter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.minturtle.careersupport.common.service.ChatService;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = OpenAiAutoConfiguration.class)
public abstract class IntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;

    @MockBean
    protected ChatService chatService;


}