package org.minturtle.careersupport.codereview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minturtle.careersupport.codereview.dto.CodeReviewRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class StubAiCodeReviewService implements CodeReviewService{

    private final AiCodeReviewClient aiCodeReviewClient;
    private static final List<AiCodeReviewClient.ReviewRequest> codeReviewRequests;

    @Override
    public Mono<Void> doCodeReview(CodeReviewRequest codeReviewRequest) {
        aiCodeReviewClient.getAiCodeReview(codeReviewRequests).subscribe(
            res-> log.info(res.getReviewContent())
        );

        return Mono.empty();
    }


    static{
        String patch1 = """
                @@ -1,5 +1,7 @@
                 package org.minturtle.careersupport.auth.filter;
                \s
                +import lombok.RequiredArgsConstructor;
                +import org.minturtle.careersupport.auth.utils.ApiTokenProvider;
                 import org.minturtle.careersupport.user.dto.UserInfoDto;
                 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
                 import org.springframework.security.core.context.ReactiveSecurityContextHolder;
                @@ -11,16 +13,25 @@
                \s
                \s
                 @Component
                +@RequiredArgsConstructor
                 public class ApiTokenFilter implements WebFilter {
                \s
                +    private final ApiTokenProvider apiTokenProvider;
                +    private final static String API_TOKEN_HEADER = "X-API-TOKEN";
                +
                     @Override
                     public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                -        // TODO: 실제 토큰 검증 및 사용자 정보 추출 로직 구현
                -        return Mono.just(new UserInfoDto("username", "role", "token"))
                +        String token = exchange.getRequest().getHeaders().getFirst(API_TOKEN_HEADER);
                +
                +        if (token == null || !token.startsWith("cs_")) {
                +            return chain.filter(exchange);
                +        }
                +
                +        return apiTokenProvider.decryptApiToken(token)
                                 .map(user -> new UsernamePasswordAuthenticationToken(user, null, null))
                                 .flatMap(auth -> chain.filter(exchange)
                                         .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)));
                \s
                     }
                \s
                -}
                +}
                \\ No newline at end of file]
                                
                """;

        String patch2 = """
                @@ -0,0 +1,48 @@
                +package org.minturtle.careersupport.auth.utils;
                +
                +import com.fasterxml.jackson.core.JsonProcessingException;
                +import com.fasterxml.jackson.databind.ObjectMapper;
                +import lombok.RequiredArgsConstructor;
                +import org.minturtle.careersupport.common.aop.Logging;
                +import org.minturtle.careersupport.common.exception.BadRequestException;
                +import org.minturtle.careersupport.common.exception.InternalServerException;
                +import org.minturtle.careersupport.common.utils.ReactiveEncryptUtils;
                +import org.minturtle.careersupport.user.dto.UserInfoDto;
                +import org.springframework.stereotype.Component;
                +import reactor.core.publisher.Mono;
                +
                +
                +@Component
                +@RequiredArgsConstructor
                +@Logging
                +public class ApiTokenProvider {
                +
                +    private final ReactiveEncryptUtils encryptUtils;
                +    private final ObjectMapper objectMapper;
                +
                +    public Mono<String> generate(UserInfoDto userInfoDto){
                +        try {
                +            return encryptUtils.encrypt(objectMapper.writeValueAsString(userInfoDto))
                +                    .map(it-> "cs_" + it);
                +        }catch (JsonProcessingException e){
                +            throw new InternalServerException("API 토큰 생성 중 예상치 못한 오류가 발생했습니다.", e);
                +        }
                +
                +
                +    }
                +
                +    public Mono<UserInfoDto> decryptApiToken(String token) {
                +        if (token == null || !token.startsWith("cs_")) {
                +            return Mono.error(new BadRequestException("토큰이 올바르지 않습니다."));
                +        }
                +
                +        return encryptUtils.decrypt(token.substring(3)).map(decrypted -> {
                +            try {
                +                return objectMapper.readValue(decrypted, UserInfoDto.class);
                +            } catch (JsonProcessingException e) {
                +                throw new InternalServerException("API 토큰 해독 중 예상치 못한 오류가 발생했습니다.");
                +            }
                +        });
                +
                +    }
                +}
                \\ No newline at end of file]
                                
                """;

        codeReviewRequests = List.of(
                new AiCodeReviewClient.ReviewRequest(AiCodeReviewClient.ReviewFileStatus.modified, "src/main/java/org/minturtle/careersupport/auth/filter/ApiTokenFilter.java", patch1),
                new AiCodeReviewClient.ReviewRequest(AiCodeReviewClient.ReviewFileStatus.modified, "src/main/java/org/minturtle/careersupport/common/config/SecurityConfig.java" , patch2)
        );
    }
}