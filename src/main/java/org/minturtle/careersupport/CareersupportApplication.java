package org.minturtle.careersupport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import reactor.blockhound.BlockHound;

@SpringBootApplication
@EnableWebFluxSecurity
public class CareersupportApplication {

	public static void main(String[] args) {
		BlockHound.install();
		SpringApplication.run(CareersupportApplication.class, args);
	}

}