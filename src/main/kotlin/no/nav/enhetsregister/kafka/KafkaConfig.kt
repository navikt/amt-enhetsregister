package no.nav.enhetsregister.kafka

import no.nav.common.kafka.producer.KafkaProducerClientImpl
import no.nav.common.kafka.util.KafkaPropertiesPreset
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.Properties

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KafkaTopicProperties::class)
class KafkaConfig {

	@Bean
	@Profile("default")
	fun kafkaConsumerProperties(): KafkaProperties = object : KafkaProperties {

		// ikke i bruk
		override fun consumer(): Properties =
			KafkaPropertiesPreset.aivenDefaultConsumerProperties("amt-enhetsregister-v1")

		override fun producer(): Properties =
			KafkaPropertiesPreset.aivenByteProducerProperties("amt-enhetsregister")
	}

	@Bean
	fun kafkaProducer(kafkaProperties: KafkaProperties) =
		KafkaProducerClientImpl<ByteArray, ByteArray>(kafkaProperties.producer())
}
