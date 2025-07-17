package no.nav.amt_enhetsregister.service

import no.nav.amt_enhetsregister.kafka.VirksomhetV1Dto
import no.nav.amt_enhetsregister.test_utils.IntegrationTest
import no.nav.amt_enhetsregister.test_utils.KafkaMessageConsumer
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KafkaProducerTest(
	private val kafkaProducerService: KafkaProducerService,
	private val kafkaMessageConsumer: KafkaMessageConsumer
) : IntegrationTest() {

	@Test
	fun `skal publisere virksomhet på topic`() {
		val virksomhetV1Dto = VirksomhetV1Dto(
			organisasjonsnummer = "123456789",
			navn = "Orgnavn",
			overordnetEnhetOrganisasjonsnummer = "987654321",
		)
		kafkaProducerService.publiserVirksomhet(virksomhetV1Dto)

		await().untilAsserted {
			val record = kafkaMessageConsumer.getLatestRecord(KafkaMessageConsumer.Topic.VIRKSOMHETER)
			assertEquals(record?.key(), virksomhetV1Dto.organisasjonsnummer)
			val expectedJson = """
				{"organisasjonsnummer":"123456789","navn":"Orgnavn","overordnetEnhetOrganisasjonsnummer":"987654321"}
			""".trimIndent()
			assertEquals(record?.value(), expectedJson)
		}
	}
}
