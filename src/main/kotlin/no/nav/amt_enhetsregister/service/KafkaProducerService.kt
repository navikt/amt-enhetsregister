package no.nav.amt_enhetsregister.service

import no.nav.amt_enhetsregister.kafka.KafkaTopicProperties
import no.nav.amt_enhetsregister.kafka.VirksomhetV1Dto
import no.nav.amt_enhetsregister.utils.JsonUtils
import no.nav.common.kafka.producer.KafkaProducerClient
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
	private val kafkaProducerClient: KafkaProducerClient<ByteArray, ByteArray>,
	private val kafkaTopicProperties: KafkaTopicProperties
) {
	fun publiserVirksomhet(virksomhetDto: VirksomhetV1Dto) {
		val key = virksomhetDto.organisasjonsnummer.toByteArray()
		val value = JsonUtils.toJsonString(virksomhetDto).toByteArray()
		val record = ProducerRecord(kafkaTopicProperties.virksomheterTopic, key, value)
		kafkaProducerClient.sendSync(record)
	}
}
