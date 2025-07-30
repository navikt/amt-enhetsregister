package no.nav.amt_enhetsregister.client

data class Underenhet(
	val organisasjonsnummer: String,
	val navn: String,
	val slettedato: String?,
	val overordnetEnhet: String?,
)
