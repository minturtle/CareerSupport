package org.minturtle.careersupport.common.filter.logging;

import lombok.extern.slf4j.Slf4j;
import org.minturtle.careersupport.common.dto.RequestInfo;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        RequestInfo requestInfo = new RequestInfo();
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        log.info("[{}] {} {} Requested", requestInfo.getRequestId(), method, path);
        Instant start = Instant.now();
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put("REQUEST_INFO", requestInfo))
                .doFinally(v -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info(
                            "[{}] {} {} Responsed {} at {}ms",
                            requestInfo.getRequestId(),
                            method,
                            path,
                            exchange.getResponse().getStatusCode(),
                            duration.toMillis()
                    );
                });
    }

}