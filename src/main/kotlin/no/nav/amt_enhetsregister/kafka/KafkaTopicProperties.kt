package no.nav.amt_enhetsregister.kafka

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.env")
data class KafkaTopicProperties(
	val virksomheterTopic: String,
)
