package no.nav.amt_enhetsregister.kafka

import java.util.*

interface KafkaProperties {

	fun consumer(): Properties

    fun producer(): Properties

}
