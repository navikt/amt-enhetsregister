package no.nav.amt_enhetsregister.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt_enhetsregister.kafka.KafkaTopicProperties
import no.nav.amt_enhetsregister.kafka.VirksomhetV1Dto
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
	private val kafkaProducerClient: KafkaProducerClient<ByteArray, ByteArray>,
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val objectMapper: ObjectMapper
) {
	fun publiserVirksomhet(virksomhetDto: VirksomhetV1Dto) {
		val key = virksomhetDto.organisasjonsnummer.toByteArray()
		val value = objectMapper.writeValueAsBytes(virksomhetDto)
		val record = ProducerRecord(kafkaTopicProperties.virksomheterTopic, key, value)
		kafkaProducerClient.sendSync(record)
	}
}
