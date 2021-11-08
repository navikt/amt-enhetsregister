package no.nav.amt_enhetsregister.config

import no.nav.amt_enhetsregister.client.BronnoysundClient
import no.nav.amt_enhetsregister.client.HentModerenhetPage
import no.nav.amt_enhetsregister.client.HentUnderenhetPage
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LocalApplicationConfig {

	@Bean
	fun bronnoysundClient(): BronnoysundClient {
		return object : BronnoysundClient {
			override fun hentModerenheterPage(page: Int, size: Int): HentModerenhetPage {
				TODO("Not yet implemented")
			}

			override fun hentUnderenheterPage(page: Int, size: Int): HentUnderenhetPage {
				TODO("Not yet implemented")
			}
		}
	}

}
