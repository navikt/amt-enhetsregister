package no.nav.amt_enhetsregister.kafka

data class VirksomhetV1Dto(
	val organisasjonsnummer: String,
	val navn: String,
	val overordnetEnhetOrganisasjonsnummer: String?,
)
