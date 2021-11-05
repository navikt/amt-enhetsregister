package no.nav.amt_enhetsregister.config

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!local")
@EnableJwtTokenValidation
class ApplicationConfig {


}
