package org.minturtle.careersupport.common.aop

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.minturtle.careersupport.common.dto.RequestInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

@Aspect
@Component
class LoggingAspect {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Around("@within(org.minturtle.careersupport.common.aop.Logging) || @annotation(org.minturtle.careersupport.common.aop.Logging)")
    fun logAround(joinPoint: ProceedingJoinPoint): Any {
        val methodName = joinPoint.signature.name
        val className = joinPoint.target.javaClass.simpleName
        val start = Instant.now()

        return when (val result = joinPoint.proceed()) {
            is Mono<*> -> proceedMono(result, className, methodName, start)
            is Flux<*> -> proceedFlux(result, className, methodName, start)
            else -> proceedNormalObject(result, className, methodName, start)
        }
    }

    private fun proceedMono(mono: Mono<*>, className: String, methodName: String, startTime: Instant): Any {
        return Mono.deferContextual { ctx ->
            val requestInfo = ctx.getOrDefault("REQUEST_INFO", RequestInfo())!!
            log.info("[{}] {}.{} started", requestInfo.requestId, className, methodName)

            mono.doOnSuccess {
                logCompletion(requestInfo, className, methodName, startTime, null)
            }.doOnError { err ->
                logCompletion(requestInfo, className, methodName, startTime, err)
            }
        }
    }

    private fun proceedFlux(flux: Flux<*>, className: String, methodName: String, startTime: Instant): Flux<*> {
        return Flux.deferContextual { ctx ->
            val requestInfo = ctx.getOrDefault("REQUEST_INFO", RequestInfo())!!
            log.info("[{}] {}.{} started", requestInfo.requestId, className, methodName)

            flux.doOnComplete {
                logCompletion(requestInfo, className, methodName, startTime, null)
            }.doOnError { err ->
                logCompletion(requestInfo, className, methodName, startTime, err)
            }
        }
    }

    private fun proceedNormalObject(obj: Any, className: String, methodName: String, startTime: Instant): Any {
        return mono {
            val requestInfo = RequestInfo()
            log.info("[{}] {}.{} started", requestInfo.requestId, className, methodName)

            try {
                obj.also {
                    logCompletion(requestInfo, className, methodName, startTime, null)
                }
            } catch (err: Throwable) {
                logCompletion(requestInfo, className, methodName, startTime, err)
                throw err
            }
        }
    }

    private fun logCompletion(requestInfo: RequestInfo, className: String, methodName: String, start: Instant, error: Throwable?) {
        val duration = Duration.between(start, Instant.now())
        if (error == null) {
            log.info(
                "[{}] {}.{} completed, {}ms",
                requestInfo.requestId, className, methodName, duration.toMillis()
            )
        } else {
            log.info(
                "[{}] {}.{} failed, {}ms - {}",
                requestInfo.requestId, className, methodName, duration.toMillis(), error.message
            )
        }
    }
}