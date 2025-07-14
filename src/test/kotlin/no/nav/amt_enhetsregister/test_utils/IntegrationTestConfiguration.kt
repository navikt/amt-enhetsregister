package no.nav.amt_enhetsregister.test_utils

import no.nav.amt_enhetsregister.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class IntegrationTestConfiguration {

	@Bean
	fun kafkaProperties(): KafkaProperties = SingletonKafkaContainer.getKafkaProperties()
}
