package no.nav.amt_enhetsregister.repository.type

data class Enhet(
	val id: Int,
	val organisasjonsnummer: String,
	val navn: String,
	val overordnetEnhet: String?
)
