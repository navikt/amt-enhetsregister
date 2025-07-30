package no.nav.amt_enhetsregister.client

import java.time.ZonedDateTime

data class EnhetOppdatering(
	val oppdateringId: Int,
	val dato: ZonedDateTime,
	val organisasjonsnummer: String,
	val endringstype: EnhetOppdateringType,
)
