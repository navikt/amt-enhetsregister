package no.nav.amt_enhetsregister.client

interface BronnoysundClient {

	fun hentModerenheterPage(page: Int, size: Int): HentModerenhetPage

	fun hentUnderenheterPage(page: Int, size: Int): HentUnderenhetPage

}

data class HentModerenhetPage(
	val moderenheter: List<Moderenhet>,
	val page: EnhetPage
) {
	data class Moderenhet(
		val organisasjonsnummer: String,
		val navn: String,
	)
}

data class HentUnderenhetPage(
	val underenheter: List<Underenhet>,
	val page: EnhetPage
) {
	data class Underenhet(
		val organisasjonsnummer: String,
		val navn: String,
		val overordnetEnhet: String
	)
}

data class EnhetPage(
	val size: Int,
	val totalElements: Int,
	val totalPages: Int,
	val number: Int
)


