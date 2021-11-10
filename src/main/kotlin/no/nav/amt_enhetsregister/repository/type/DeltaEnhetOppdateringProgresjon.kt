package no.nav.amt_enhetsregister.repository.type

import java.time.ZonedDateTime

data class DeltaEnhetOppdateringProgresjon(
	val oppdateringId: Int,
	val enhetType: EnhetType,
	val sisteOppdatering: ZonedDateTime
)
