package no.nav.amt_enhetsregister.config

import no.nav.amt_enhetsregister.client.BronnoysundClient
import no.nav.amt_enhetsregister.client.EnhetOppdatering
import no.nav.amt_enhetsregister.client.Moderenhet
import no.nav.amt_enhetsregister.client.Underenhet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LocalApplicationConfig {

	@Bean
	fun bronnoysundClient(): BronnoysundClient = object : BronnoysundClient {
		override fun hentModerenhetOppdateringer(fraOppdateringId: Int, size: Int): List<EnhetOppdatering> {
			TODO("Not yet implemented")
		}

		override fun hentUnderenhetOppdateringer(fraOppdateringId: Int, size: Int): List<EnhetOppdatering> {
			TODO("Not yet implemented")
		}

		override fun hentModerenhet(organisasjonsnummer: String): Moderenhet {
			TODO("Not yet implemented")
		}

		override fun hentUnderenhet(organisasjonsnummer: String): Underenhet {
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

