package no.nav.amt_enhetsregister.client

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt_enhetsregister.utils.JsonUtils.getObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request

class BronnoysundClientImpl(
	private val bronnoysundUrl: String = BRONNOYSUND_URL,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val objectMapper: ObjectMapper = getObjectMapper(),
) : BronnoysundClient {

	companion object {
		const val BRONNOYSUND_URL = "https://data.brreg.no"
	}

	override fun hentModerenheterPage(page: Int, size: Int): HentModerenhetPage {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/enheter?page=$page&size=$size")
			.header("Accept", "application/json")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente enheter page=$page size=$size. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val dto = objectMapper.readValue(body, HentEnheterDto::class.java)

			return mapTilHentEnheterPage(dto)
		}
	}

	override fun hentUnderenheterPage(page: Int, size: Int): HentUnderenhetPage {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/underenheter?page=$page&size=$size")
			.header("Accept", "application/json")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente enheter page=$page size=$size. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val dto = objectMapper.readValue(body, HentUnderenheterDto::class.java)

			return mapTilHentUnderenheterPage(dto)
		}
	}

	private fun mapTilHentEnheterPage(dto: HentEnheterDto): HentModerenhetPage {
		return HentModerenhetPage(
			enheter = dto._embedded.enheter.map { HentModerenhetPage.Moderenhet(
				organisasjonsnummer = it.organisasjonsnummer,
				navn = it.navn
			) },
			page = HentModerenhetPage.Page(
				size = dto.page.size,
				totalElements = dto.page.totalElements,
				totalPages = dto.page.totalPages,
				number = dto.page.number
			)
		)
	}

	private fun mapTilHentUnderenheterPage(dto: HentUnderenheterDto): HentUnderenhetPage {
		return HentUnderenhetPage(
			underenheter = dto._embedded.enheter.map { HentUnderenhetPage.Underenhet(
				organisasjonsnummer = it.organisasjonsnummer,
				navn = it.navn,
				overordnetEnhet = it.overordnetEnhet
			) },
			page = HentUnderenhetPage.Page(
				size = dto.page.size,
				totalElements = dto.page.totalElements,
				totalPages = dto.page.totalPages,
				number = dto.page.number
			)
		)
	}

}

private data class HentEnheterDto(
	val _embedded: Embedded,
	val page: Page
) {
	data class Embedded(
		val enheter: List<Enhet>
	) {
		data class Enhet(
			val organisasjonsnummer: String,
			val navn: String,
		)
	}

	data class Page(
		val size: Int,
		val totalElements: Int,
		val totalPages: Int,
		val number: Int
	)
}

private data class HentUnderenheterDto(
	val _embedded: Embedded,
	val page: Page
) {
	data class Embedded(
		val enheter: List<Underenhet>
	) {
		data class Underenhet(
			val organisasjonsnummer: String,
			val navn: String,
			val overordnetEnhet: String
		)
	}

	data class Page(
		val size: Int,
		val totalElements: Int,
		val totalPages: Int,
		val number: Int
	)
}


