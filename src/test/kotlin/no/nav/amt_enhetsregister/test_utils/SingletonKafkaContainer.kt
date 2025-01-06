package no.nav.amt_enhetsregister.test_utils

import no.nav.amt_enhetsregister.kafka.KafkaProperties
import no.nav.common.kafka.util.KafkaPropertiesBuilder
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.slf4j.LoggerFactory
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

object SingletonKafkaContainer {

	private const val producerId = "INTEGRATION_PRODUCER"
	private const val consumerId = "INTEGRATION_CONSUMER"

	private val log = LoggerFactory.getLogger(javaClass)

	private var kafkaContainer: KafkaContainer? = null


	fun getKafkaProperties(): KafkaProperties {
		val host = getHost()

		val properties = object : KafkaProperties {
			override fun consumer(): Properties {
				return KafkaPropertiesBuilder.consumerBuilder()
					.withBrokerUrl(host)
					.withBaseProperties()
					.withConsumerGroupId(consumerId)
					.withDeserializers(ByteArrayDeserializer::class.java, ByteArrayDeserializer::class.java)
					.build()
			}

			override fun producer(): Properties {
				return KafkaPropertiesBuilder.producerBuilder()
					.withBrokerUrl(host)
					.withBaseProperties()
					.withProducerId(producerId)
					.withSerializers(ByteArraySerializer::class.java, ByteArraySerializer::class.java)
					.build()
			}
		}

		return properties
	}

	private fun getHost(): String {
		if (kafkaContainer == null) {
			log.info("Starting new Kafka Instance...")
			kafkaContainer = KafkaContainer(DockerImageName.parse("apache/kafka"))
				.withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094")
				// workaround for https://github.com/testcontainers/testcontainers-java/issues/9506
				.apply {
					start()
					System.setProperty("KAFKA_BROKERS", bootstrapServers)
				}
			setupShutdownHook()
		}
		return kafkaContainer!!.bootstrapServers
	}

	private fun setupShutdownHook() {
		Runtime.getRuntime().addShutdownHook(Thread {
			log.info("Shutting down Kafka server...")
			kafkaContainer?.stop()
		})
	}
}
