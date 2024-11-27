package org.minturtle.careersupport.user.resolvers

import org.minturtle.careersupport.user.dto.UserInfoDto
import org.minturtle.careersupport.user.resolvers.annotations.CurrentUser
import org.springframework.core.MethodParameter
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class UserInfoArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return (parameter.getParameterAnnotation(CurrentUser::class.java)
            != null && parameter.getParameterType() == UserInfoDto::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any> {
        return ReactiveSecurityContextHolder.getContext()
            .map { obj: SecurityContext -> obj.authentication }
            .filter { auth: Authentication? -> auth != null && auth.principal is UserInfoDto }
            .map { obj: Authentication -> obj.principal }
    }

}