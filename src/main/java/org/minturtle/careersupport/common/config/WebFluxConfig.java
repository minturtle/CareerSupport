package org.minturtle.careersupport.common.config;

import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.user.resolvers.UserInfoArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebFluxConfig implements WebFluxConfigurer {

    private final UserInfoArgumentResolver userInfoArgumentResolver;


    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(userInfoArgumentResolver);
    }
}

