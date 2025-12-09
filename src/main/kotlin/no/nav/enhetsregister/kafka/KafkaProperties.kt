package no.nav.enhetsregister.kafka

import java.util.Properties

interface KafkaProperties {
	fun consumer(): Properties
	fun producer(): Properties
}
