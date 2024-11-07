package org.minturtle.careersupport.common.filter.logging

import lombok.extern.slf4j.Slf4j
import org.minturtle.careersupport.common.dto.RequestInfo
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.util.context.Context
import java.time.Duration
import java.time.Instant


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class LoggingFilter : WebFilter {

    private val log = LoggerFactory.getLogger(LoggingFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val requestInfo = RequestInfo()
        val path = exchange.request.path.value()
        val method = exchange.request.method.name()
        log.info("[{}] {} {} Requested", requestInfo.requestId, method, path)
        val start = Instant.now()
        return chain.filter(exchange)
            .contextWrite { ctx: Context ->
                ctx.put(
                    "REQUEST_INFO",
                    requestInfo
                )
            }
            .doFinally { v: SignalType? ->
                val duration = Duration.between(start, Instant.now())
                log.info(
                    "[{}] {} {} Responsed {} at {}ms",
                    requestInfo.requestId,
                    method,
                    path,
                    exchange.response.statusCode,
                    duration.toMillis()
                )
            }
    }
}