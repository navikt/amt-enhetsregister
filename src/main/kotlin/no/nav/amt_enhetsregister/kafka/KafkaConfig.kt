package no.nav.amt_enhetsregister.kafka

import no.nav.common.kafka.producer.KafkaProducerClient
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import no.nav.common.kafka.util.KafkaPropertiesPreset
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.Properties

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties::class)
class KafkaConfig {

	@Bean
	@Profile("default")
	fun kafkaConsumerProperties(): KafkaProperties {

		return object : KafkaProperties {
			override fun consumer(): Properties {
				return KafkaPropertiesPreset.aivenDefaultConsumerProperties("amt-enhetsregister-v1")
			}

			override fun producer(): Properties {
				return KafkaPropertiesPreset.aivenByteProducerProperties("amt-enhetsregister")
			}
		}
	}

	@Bean
	fun kafkaProducer(kafkaProperties: KafkaProperties): KafkaProducerClient<ByteArray, ByteArray> {
		return KafkaProducerClientImpl(kafkaProperties.producer())
	}


}
