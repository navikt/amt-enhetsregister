package no.nav.enhetsregister.kafka

import java.util.*

interface KafkaProperties {

	fun consumer(): Properties

    fun producer(): Properties

}
