package no.nav.amt_enhetsregister.controller.dto

import no.nav.amt_enhetsregister.repository.type.Enhet

data class EnhetDto(
	val organisasjonsnummer: String,
	val navn: String,
	val overordnetEnhet: String?
)

fun Enhet.tilDto(): EnhetDto {
	return EnhetDto(
		organisasjonsnummer = this.organisasjonsnummer,
		navn = this.navn,
		overordnetEnhet = this.overordnetEnhet
	)
}
