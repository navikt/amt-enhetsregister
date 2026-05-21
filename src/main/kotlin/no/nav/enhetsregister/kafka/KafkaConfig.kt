package no.nav.enhetsregister.kafka

import no.nav.common.kafka.producer.KafkaProducerClientImpl
import no.nav.common.kafka.util.KafkaPropertiesBuilder
import no.nav.common.kafka.util.KafkaPropertiesPreset
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
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
	@Profile("local")
	fun kafkaLocalProperties(): KafkaProperties = object : KafkaProperties {
		override fun consumer(): Properties = KafkaPropertiesBuilder.consumerBuilder()
			.withBrokerUrl("localhost:9092")
			.withBaseProperties()
			.withConsumerGroupId("amt-enhetsregister-local-consumer")
			.withDeserializers(ByteArrayDeserializer::class.java, ByteArrayDeserializer::class.java)
			.build()

		override fun producer(): Properties = KafkaPropertiesBuilder.producerBuilder()
			.withBrokerUrl("localhost:9092")
			.withBaseProperties()
			.withProducerId("amt-enhetsregister-local-producer")
			.withSerializers(ByteArraySerializer::class.java, ByteArraySerializer::class.java)
			.build()
	}

	@Bean
	fun kafkaProducer(kafkaProperties: KafkaProperties) =
		KafkaProducerClientImpl<ByteArray, ByteArray>(kafkaProperties.producer())
}
