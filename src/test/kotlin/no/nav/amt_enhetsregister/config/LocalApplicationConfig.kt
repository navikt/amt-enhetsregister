package no.nav.amt_enhetsregister.config

import no.nav.amt_enhetsregister.client.*
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

			override fun hentAlleModerenheter(): List<Moderenhet> {
				TODO("Not yet implemented")
			}

			override fun hentAlleUnderenheter(): List<Underenhet> {
				TODO("Not yet implemented")
			}
		}
	}

}
