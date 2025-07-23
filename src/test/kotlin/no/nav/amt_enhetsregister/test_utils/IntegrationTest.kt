package no.nav.amt_enhetsregister.test_utils

import no.nav.amt_enhetsregister.repository.RepositoryTestBase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationTest : RepositoryTestBase() {

	companion object {
		val oAuthServer = MockOAuthServer()

		@JvmStatic
		@DynamicPropertySource
		@Suppress("unused")
		fun registerProperties(registry: DynamicPropertyRegistry) {
			oAuthServer.start()

			registry.add("no.nav.security.jwt.issuer.azuread.discovery-url", oAuthServer::getDiscoveryUrl)
			registry.add("no.nav.security.jwt.issuer.azuread.accepted-audience") { "test-aud" }
		}
	}
}
