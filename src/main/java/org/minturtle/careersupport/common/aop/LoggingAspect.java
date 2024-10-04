package org.minturtle.careersupport.common.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.minturtle.careersupport.common.dto.RequestInfo;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("@within(org.minturtle.careersupport.common.aop.Logging) || @annotation(org.minturtle.careersupport.common.aop.Logging)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        Object result = joinPoint.proceed();
        Instant start = Instant.now();
        if(result instanceof Mono<?> mono){
            return proceedMono(mono, className, methodName, start);
        }
        else if(result instanceof Flux<?> flux){
            return proceedFlux(flux, className, methodName, start);
        }
        else{
            return proceedNormalObject(result, className, methodName, start);
        }

    }

    private Mono<?> proceedMono(Mono<?> mono, String className, String methodName, Instant startTime){
        return Mono.deferContextual(ctx->{
            RequestInfo requestInfo = ctx.getOrDefault("REQUEST_INFO", new RequestInfo());
            log.info("[{}] {}.{} started", requestInfo.getRequestId(), className, methodName);

            return mono
                    .doOnSuccess(v -> logCompletion(requestInfo, className, methodName, startTime, null))
                    .doOnError(err-> logCompletion(requestInfo, className, methodName, startTime, err));
        });
    }

    private Flux<?> proceedFlux(Flux<?> flux, String className, String methodName, Instant startTime){
        return Flux.deferContextual(ctx->{
            RequestInfo requestInfo = ctx.getOrDefault("REQUEST_INFO", new RequestInfo());
            log.info("[{}] {}.{} started", requestInfo.getRequestId(), className, methodName);

            return flux
                    .doOnComplete(()-> logCompletion(requestInfo, className, methodName, startTime, null))
                    .doOnError(err-> logCompletion(requestInfo, className, methodName, startTime, err));
        });
    }

    private Object proceedNormalObject(Object object, String className, String methodName, Instant startTime){
        return Mono.deferContextual(ctx->{
            RequestInfo requestInfo = ctx.getOrDefault("REQUEST_INFO", new RequestInfo());
            log.info("[{}] {}.{} started", requestInfo.getRequestId(), className, methodName);

            return Mono.just(object)
                    .doOnSuccess(v -> logCompletion(requestInfo, className, methodName, startTime, null))
                    .doOnError(err-> logCompletion(requestInfo, className, methodName, startTime, err));
        });
    }

    private void logCompletion(RequestInfo requestInfo, String className, String methodName, Instant start, Throwable error) {
        Duration duration = Duration.between(start, Instant.now());
        if (error == null) {
            log.info("[{}] {}.{} completed, {}ms",
                    requestInfo.getRequestId(), className, methodName, duration.toMillis());
        } else {
            log.info("[{}] {}.{} failed, {}ms - {}",
                    requestInfo.getRequestId(), className, methodName, duration.toMillis(), error.getMessage());
        }
    }

}