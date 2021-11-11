package no.nav.amt_enhetsregister.controller.dto

import no.nav.amt_enhetsregister.service.EnhetService

data class EnhetDto(
	val organisasjonsnummer: String,
	val navn: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
	val overordnetEnhetNavn: String?
)

fun EnhetService.EnhetMedOverordnetEnhet.tilDto(): EnhetDto {
	return EnhetDto(
		organisasjonsnummer = this.organisasjonsnummer,
		navn = this.navn,
		overordnetEnhetOrganisasjonsnummer = this.overordnetEnhetOrganisasjonsnummer,
		overordnetEnhetNavn = this.overordnetEnhetNavn
	)
}
