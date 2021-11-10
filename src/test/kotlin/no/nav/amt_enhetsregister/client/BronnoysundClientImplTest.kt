package no.nav.amt_enhetsregister.client

import no.nav.amt_enhetsregister.utils.ResourceUtils
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class BronnoysundClientImplTest {

	@Test
	fun `hentModerenhet skal lage riktig request og parse response`() {
		val server = MockWebServer()
		val serverUrl = server.url("").toString().dropLast(1) // Removes trailing "/"

		val client = BronnoysundClientImpl(
			bronnoysundUrl = serverUrl
		)

		val responseBody = ResourceUtils.getResourceAsText("/client/hent-moderenhet-response.json")

		server.enqueue(MockResponse().setBody(responseBody))

		val moderenhet = client.hentModerenhet("998877443")

		val request = server.takeRequest()

		assertEquals("$serverUrl/enhetsregisteret/api/enheter/998877443", request.requestUrl.toString())

		assertEquals("998877443", moderenhet.organisasjonsnummer)
		assertEquals("Moderenhet 1", moderenhet.navn)
	}

	@Test
	fun `hentUnderenhet skal lage riktig request og parse response`() {
		val server = MockWebServer()
		val serverUrl = server.url("").toString().dropLast(1) // Removes trailing "/"

		val client = BronnoysundClientImpl(
			bronnoysundUrl = serverUrl
		)

		val responseBody = ResourceUtils.getResourceAsText("/client/hent-underenhet-response.json")

		server.enqueue(MockResponse().setBody(responseBody))

		val underenhet = client.hentUnderenhet("12345678")

		val request = server.takeRequest()

		assertEquals("$serverUrl/enhetsregisteret/api/underenheter/12345678", request.requestUrl.toString())

		assertEquals("12345678", underenhet.organisasjonsnummer)
		assertEquals("Underenhet 1", underenhet.navn)
		assertEquals("9988564", underenhet.overordnetEnhet)
	}

	@Test
	fun `hentModerenhetOppdateringer skal lage riktig request og parse response`() {
		val server = MockWebServer()
		val serverUrl = server.url("").toString().dropLast(1) // Removes trailing "/"

		val client = BronnoysundClientImpl(
			bronnoysundUrl = serverUrl
		)

		val responseBody = ResourceUtils.getResourceAsText("/client/hent-moderenhet-oppdateringer-response.json")

		server.enqueue(MockResponse().setBody(responseBody))

		val oppdateringer = client.hentModerenhetOppdateringer(10, 100)

		val request = server.takeRequest()

		assertEquals("$serverUrl/enhetsregisteret/api/oppdateringer/enheter?oppdateringsid=10&size=100", request.requestUrl.toString())

		assertEquals(2, oppdateringer.size)

		val expectedDate = ZonedDateTime.of(
			LocalDateTime.of(2018, 5, 14, 21, 53, 0),
			ZoneOffset.UTC
		)

		assertEquals(123867, oppdateringer[0].oppdateringId)
		assertEquals(expectedDate, oppdateringer[0].dato)
		assertEquals("9876543", oppdateringer[0].organisasjonsnummer)
		assertEquals(EnhetOppdateringType.UKJENT, oppdateringer[0].endringstype)

		assertEquals(123868, oppdateringer[1].oppdateringId)
		assertEquals(expectedDate, oppdateringer[1].dato)
		assertEquals("88445533", oppdateringer[1].organisasjonsnummer)
		assertEquals(EnhetOppdateringType.NY, oppdateringer[1].endringstype)
	}

	@Test
	fun `hentUnderenhetOppdateringer skal lage riktig request og parse response`() {
		val server = MockWebServer()
		val serverUrl = server.url("").toString().dropLast(1) // Removes trailing "/"

		val client = BronnoysundClientImpl(
			bronnoysundUrl = serverUrl
		)

		val responseBody = ResourceUtils.getResourceAsText("/client/hent-underenhet-oppdateringer-response.json")

		server.enqueue(MockResponse().setBody(responseBody))

		val oppdateringer = client.hentUnderenhetOppdateringer(10, 100)

		val request = server.takeRequest()

		assertEquals("$serverUrl/enhetsregisteret/api/oppdateringer/underenheter?oppdateringsid=10&size=100", request.requestUrl.toString())

		assertEquals(2, oppdateringer.size)

		val expectedDate = ZonedDateTime.of(
			LocalDateTime.of(2018, 5, 14, 21, 53, 0),
			ZoneOffset.UTC
		)

		assertEquals(123867, oppdateringer[0].oppdateringId)
		assertEquals(expectedDate, oppdateringer[0].dato)
		assertEquals("9876543", oppdateringer[0].organisasjonsnummer)
		assertEquals(EnhetOppdateringType.UKJENT, oppdateringer[0].endringstype)

		assertEquals(123868, oppdateringer[1].oppdateringId)
		assertEquals(expectedDate, oppdateringer[1].dato)
		assertEquals("88445533", oppdateringer[1].organisasjonsnummer)
		assertEquals(EnhetOppdateringType.NY, oppdateringer[1].endringstype)
	}

}
