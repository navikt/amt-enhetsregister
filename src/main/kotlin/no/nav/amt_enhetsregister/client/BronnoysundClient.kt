package no.nav.amt_enhetsregister.client

interface BronnoysundClient {

	fun hentEnheterPage(page: Int, size: Int): HentEnheterPage

	fun hentUnderenheterPage(page: Int, size: Int): HentUnderenheterPage

}

data class HentEnheterPage(
	val enheter: List<Enhet>,
	val page: Page
) {
	data class Enhet(
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

data class HentUnderenheterPage(
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


