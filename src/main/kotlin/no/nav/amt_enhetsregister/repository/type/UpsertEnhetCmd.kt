package no.nav.amt_enhetsregister.repository.type

data class UpsertEnhetCmd(
	val organisasjonsnummer: String,
	val navn: String,
	val overordnetEnhet: String? = null
)
