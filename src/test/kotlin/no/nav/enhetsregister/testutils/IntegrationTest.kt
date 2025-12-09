package no.nav.enhetsregister.testutils

import no.nav.enhetsregister.repository.RepositoryTestBase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(KafkaTestConfiguration::class)
abstract class IntegrationTest : RepositoryTestBase() {

	companion object {
		val oAuthServer = MockOAuthServer()

		val kafkaContainer = KafkaContainer(DockerImageName.parse("apache/kafka")).apply {
			// workaround for https://github.com/testcontainers/testcontainers-java/issues/9506
			withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094")
			start()
			System.setProperty("KAFKA_BROKERS", bootstrapServers)
		}

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
