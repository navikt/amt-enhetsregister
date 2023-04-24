package no.nav.amt_enhetsregister.client

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt_enhetsregister.utils.JsonUtils.getObjectMapper
import no.nav.amt_enhetsregister.utils.JsonUtils.listCollectionType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpStatus
import java.time.ZonedDateTime
import java.util.zip.GZIPInputStream

class BronnoysundClientImpl(
	private val bronnoysundUrl: String = BRONNOYSUND_URL,
	private val httpClient: OkHttpClient = OkHttpClient(),
	private val objectMapper: ObjectMapper = getObjectMapper(),
) : BronnoysundClient {

	companion object {
		const val BRONNOYSUND_URL = "https://data.brreg.no"
	}

	override fun hentModerenhetOppdateringer(fraOppdateringId: Int, size: Int): List<EnhetOppdatering> {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/oppdateringer/enheter?oppdateringsid=$fraOppdateringId&size=$size")
			.header("Accept", "application/json")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente moderenhet oppdateringer. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val oppdateringer = objectMapper.readValue(body, HentModerenhetOppdateringerResponse::class.java)

			if (oppdateringer._embedded == null) {
				return emptyList()
			}

			return oppdateringer._embedded.oppdaterteEnheter.map {
				EnhetOppdatering(
					oppdateringId = it.oppdateringsid,
					organisasjonsnummer = it.organisasjonsnummer,
					endringstype = mapTilEnhetOppdateringType(it.endringstype),
					dato = ZonedDateTime.parse(it.dato)
				)
			}
		}
	}

	override fun hentUnderenhetOppdateringer(fraOppdateringId: Int, size: Int): List<EnhetOppdatering> {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/oppdateringer/underenheter?oppdateringsid=$fraOppdateringId&size=$size")
			.header("Accept", "application/json")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente underenhet oppdateringer. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val oppdateringer = objectMapper.readValue(body, HentUnderenhetOppdateringerResponse::class.java)

			if (oppdateringer._embedded == null) {
				return emptyList()
			}

			return oppdateringer._embedded.oppdaterteUnderenheter.map {
				EnhetOppdatering(
					oppdateringId = it.oppdateringsid,
					organisasjonsnummer = it.organisasjonsnummer,
					endringstype = mapTilEnhetOppdateringType(it.endringstype),
					dato = ZonedDateTime.parse(it.dato)
				)
			}
		}
	}

	override fun hentModerenhet(organisasjonsnummer: String): Moderenhet? {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/enheter/$organisasjonsnummer")
			.header("Accept", "application/json")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == HttpStatus.GONE.value() || response.code == HttpStatus.NOT_FOUND.value()) {
				return null
			}

			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente moderenhet for orgnummer $organisasjonsnummer. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val moderenhetResponse = objectMapper.readValue(body, HentModerenhetResponse::class.java)

			return Moderenhet(
				organisasjonsnummer = moderenhetResponse.organisasjonsnummer,
				navn = moderenhetResponse.navn,
				slettedato = moderenhetResponse.slettedato
			)
		}
	}

	override fun hentUnderenhet(organisasjonsnummer: String): Underenhet? {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/underenheter/$organisasjonsnummer")
			.header("Accept", "application/json")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == HttpStatus.GONE.value()) {
				return null
			}

			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente underenhet for orgnummer $organisasjonsnummer. Status: ${response.code}")
			}

			val body = response.body?.string() ?: throw RuntimeException("Body is missing")

			val underenhetResponse = objectMapper.readValue(body, HentUnderenhetResponse::class.java)

			return Underenhet(
				organisasjonsnummer = underenhetResponse.organisasjonsnummer,
				navn = underenhetResponse.navn,
				slettedato = underenhetResponse.slettedato,
				overordnetEnhet = underenhetResponse.overordnetEnhet
			)
		}
	}

	override fun hentAlleModerenheter(): List<Moderenhet> {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/enheter/lastned")
			.header("Accept", "application/vnd.brreg.enhetsregisteret.enhet.v1+gzip;charset=UTF-8")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å laste ned moderenheter. Status: ${response.code}")
			}

			val bodyStream = response.body?.byteStream() ?: throw RuntimeException("Body is missing")

			val stream = GZIPInputStream(bodyStream)

			return objectMapper.readValue(stream, listCollectionType(Moderenhet::class.java))
		}
	}

	override fun hentAlleUnderenheter(): List<Underenhet> {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/underenheter/lastned")
			.header("Accept", "application/vnd.brreg.enhetsregisteret.underenhet.v1+gzip;charset=UTF-8")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å laste ned underenheter. Status: ${response.code}")
			}

			val bodyStream = response.body?.byteStream() ?: throw RuntimeException("Body is missing")

			val stream = GZIPInputStream(bodyStream)

			return objectMapper.readValue(stream, listCollectionType(Underenhet::class.java))
		}
	}

	private fun mapTilEnhetOppdateringType(str: String): EnhetOppdateringType {
		return when(str) {
			"Ny" -> EnhetOppdateringType.NY
			"Ukjent" -> EnhetOppdateringType.UKJENT
			"Fjernet" -> EnhetOppdateringType.FJERNET
			"Sletting" -> EnhetOppdateringType.SLETTING
			"Endring" -> EnhetOppdateringType.ENDRING
			else -> throw IllegalArgumentException("Ukjent EnhetOppdateringType: $str")
		}
	}

}

private data class HentModerenhetResponse(
	val organisasjonsnummer: String,
	val navn: String,
	val slettedato: String?
)

private data class HentUnderenhetResponse(
	val organisasjonsnummer: String,
	val navn: String,
	val slettedato: String?,
	val overordnetEnhet: String? // Underenheter som ikke har "overordnetEnhet" er slettet
)

private data class HentModerenhetOppdateringerResponse(
	val _embedded: Embedded?,
) {
	data class Embedded(
		val oppdaterteEnheter: List<EnhetOppdatering>
	) {
		data class EnhetOppdatering(
			val oppdateringsid: Int,
			val dato: String,
			val organisasjonsnummer: String,
			val endringstype: String,
		)
	}
}

private data class HentUnderenhetOppdateringerResponse(
	val _embedded: Embedded?,
) {
	data class Embedded(
		val oppdaterteUnderenheter: List<EnhetOppdatering>
	) {
		data class EnhetOppdatering(
			val oppdateringsid: Int,
			val dato: String,
			val organisasjonsnummer: String,
			val endringstype: String,
		)
	}
}
