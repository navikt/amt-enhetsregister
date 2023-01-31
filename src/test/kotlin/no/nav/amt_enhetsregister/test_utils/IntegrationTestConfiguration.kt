package no.nav.amt_enhetsregister.test_utils

import no.nav.amt_enhetsregister.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class IntegrationTestConfiguration {

	@Bean
	open fun kafkaProperties(): KafkaProperties = SingletonKafkaContainer.getKafkaProperties()


}
