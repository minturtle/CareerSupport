package org.minturtle.careersupport

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity

@SpringBootApplication
@EnableWebFluxSecurity
class CareersupportApplication {

    fun main(args: Array<String>) {
        SpringApplication.run(CareersupportApplication::class.java, *args)
    }
}