package org.minturtle.careersupport.common.config

import lombok.RequiredArgsConstructor
import org.minturtle.careersupport.user.resolvers.UserInfoArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer


@Configuration
@RequiredArgsConstructor
class WebFluxConfig : WebFluxConfigurer {
    private val userInfoArgumentResolver: UserInfoArgumentResolver? = null
    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(userInfoArgumentResolver)
    }
}
