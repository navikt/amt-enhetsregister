package no.nav.amt_enhetsregister.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.zip.GZIPInputStream

@Service
class BronnoysundClient(
	@Value($$"${app.env.brreg-url}") private val bronnoysundUrl: String,
	private val objectMapper: ObjectMapper,
) {
	private val httpClient: OkHttpClient = OkHttpClient()

	fun hentModerenhetOppdateringer(fraOppdateringId: Int, size: Int): List<EnhetOppdatering> {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/oppdateringer/enheter?oppdateringsid=$fraOppdateringId&size=$size")
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente moderenhet oppdateringer. Status: ${response.code}")
			}

			val oppdateringer = objectMapper.readValue<HentModerenhetOppdateringerResponse>(response.body.string())

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

	fun hentUnderenhetOppdateringer(fraOppdateringId: Int, size: Int): List<EnhetOppdatering> {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/oppdateringer/underenheter?oppdateringsid=$fraOppdateringId&size=$size")
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente underenhet oppdateringer. Status: ${response.code}")
			}

			val oppdateringer = objectMapper.readValue<HentUnderenhetOppdateringerResponse>(response.body.string())

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

	fun hentModerenhet(organisasjonsnummer: String): Moderenhet? {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/enheter/$organisasjonsnummer")
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == HttpStatus.GONE.value() || response.code == HttpStatus.NOT_FOUND.value()) {
				return null
			}

			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente moderenhet for orgnummer $organisasjonsnummer. Status: ${response.code}")
			}

			val moderenhetResponse = objectMapper.readValue<HentModerenhetResponse>(response.body.string())

			return Moderenhet(
				organisasjonsnummer = moderenhetResponse.organisasjonsnummer,
				navn = moderenhetResponse.navn,
				slettedato = moderenhetResponse.slettedato
			)
		}
	}

	fun hentUnderenhet(organisasjonsnummer: String): Underenhet? {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/underenheter/$organisasjonsnummer")
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (response.code == HttpStatus.GONE.value() || response.code == HttpStatus.NOT_FOUND.value()) {
				return null
			}

			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å hente underenhet for orgnummer $organisasjonsnummer. Status: ${response.code}")
			}

			val underenhetResponse = objectMapper.readValue<HentUnderenhetResponse>(response.body.string())

			return Underenhet(
				organisasjonsnummer = underenhetResponse.organisasjonsnummer,
				navn = underenhetResponse.navn,
				slettedato = underenhetResponse.slettedato,
				overordnetEnhet = underenhetResponse.overordnetEnhet
			)
		}
	}

	fun hentAlleModerenheter(): List<Moderenhet> {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/enheter/lastned")
			.header("Accept", "application/vnd.brreg.enhetsregisteret.enhet.v1+gzip;charset=UTF-8")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å laste ned moderenheter. Status: ${response.code}")
			}

			val stream = GZIPInputStream(response.body.byteStream())

			return objectMapper.readValue<List<Moderenhet>>(stream)
		}
	}

	fun hentAlleUnderenheter(): List<Underenhet> {
		val request = Request.Builder()
			.url("$bronnoysundUrl/enhetsregisteret/api/underenheter/lastned")
			.header("Accept", "application/vnd.brreg.enhetsregisteret.underenhet.v1+gzip;charset=UTF-8")
			.get()
			.build()

		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				throw RuntimeException("Klarte ikke å laste ned underenheter. Status: ${response.code}")
			}

			val stream = GZIPInputStream(response.body.byteStream())

			return objectMapper.readValue<List<Underenhet>>(stream)
		}
	}

	private fun mapTilEnhetOppdateringType(str: String): EnhetOppdateringType {
		return when (str) {
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
