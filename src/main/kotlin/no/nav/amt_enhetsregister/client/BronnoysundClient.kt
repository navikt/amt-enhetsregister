package no.nav.amt_enhetsregister.client

interface BronnoysundClient {

	fun hentModerenheterPage(page: Int, size: Int): HentModerenhetPage

	fun hentUnderenheterPage(page: Int, size: Int): HentUnderenhetPage

}

data class HentModerenhetPage(
	val enheter: List<Moderenhet>,
	val page: Page
) {
	data class Moderenhet(
		val organisasjonsnummer: String,
		val navn: String,
	)

	data class Page(
		val size: Int,
		val totalElements: Int,
		val totalPages: Int,
		val number: Int
	)
}

data class HentUnderenhetPage(
	val underenheter: List<Underenhet>,
	val page: Page
) {
	data class Underenhet(
		val organisasjonsnummer: String,
		val navn: String,
		val overordnetEnhet: String
	)

	data class Page(
		val size: Int,
		val totalElements: Int,
		val totalPages: Int,
		val number: Int
	)
}


