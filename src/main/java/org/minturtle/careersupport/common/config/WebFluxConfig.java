package org.minturtle.careersupport.common.config;

import lombok.RequiredArgsConstructor;
import org.minturtle.careersupport.user.resolvers.UserInfoArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebFluxConfig implements WebFluxConfigurer {

    private final UserInfoArgumentResolver userInfoArgumentResolver;

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(userInfoArgumentResolver);
    }

}

