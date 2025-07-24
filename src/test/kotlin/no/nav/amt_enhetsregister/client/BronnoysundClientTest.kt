package no.nav.amt_enhetsregister.client

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt_enhetsregister.test_utils.ResourceUtils
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@JsonTest
class BronnoysundClientTest {

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	private lateinit var server: MockWebServer
	private lateinit var serverUrl: String
	private lateinit var client: BronnoysundClient

	@BeforeEach
	fun setup() {
		server = MockWebServer()
		serverUrl = server.url("/").toString().removeSuffix("/")

		client = BronnoysundClient(
			bronnoysundUrl = serverUrl,
			objectMapper = objectMapper,
		)
	}

	@Test
	fun `hentModerenhet skal lage riktig request og parse response`() {
		val responseBody = ResourceUtils.getResourceAsText("/client/hent-moderenhet-response.json")

		server.enqueue(MockResponse().setBody(responseBody))

		val moderenhet = client.hentModerenhet("998877443")

		val request = server.takeRequest()

		assertEquals("$serverUrl/enhetsregisteret/api/enheter/998877443", request.requestUrl.toString())

		assertNotNull(moderenhet)
		assertEquals("998877443", moderenhet?.organisasjonsnummer)
		assertEquals("Moderenhet 1", moderenhet?.navn)
	}

	@Test
	fun `hentModerenhet skal returnere null for status 410`() {
		server.enqueue(MockResponse().setResponseCode(410))

		assertNull(client.hentModerenhet("12345678"))
	}

	@Test
	fun `hentUnderenhet skal lage riktig request og parse response`() {
		val responseBody = ResourceUtils.getResourceAsText("/client/hent-underenhet-response.json")

		server.enqueue(MockResponse().setBody(responseBody))

		val underenhet = client.hentUnderenhet("12345678")

		val request = server.takeRequest()

		assertEquals("$serverUrl/enhetsregisteret/api/underenheter/12345678", request.requestUrl.toString())

		assertNotNull(underenhet)
		assertEquals("12345678", underenhet?.organisasjonsnummer)
		assertEquals("Underenhet 1", underenhet?.navn)
		assertEquals("9988564", underenhet?.overordnetEnhet)
	}

	@Test
	fun `hentUnderenhet skal returnere null for status 410`() {
		server.enqueue(MockResponse().setResponseCode(410))

		assertNull(client.hentUnderenhet("12345678"))
	}

	@Test
	fun `hentModerenhetOppdateringer skal lage riktig request og parse response`() {
		val responseBody = ResourceUtils.getResourceAsText("/client/hent-moderenhet-oppdateringer-response.json")

		server.enqueue(MockResponse().setBody(responseBody))

		val oppdateringer = client.hentModerenhetOppdateringer(10, 100)

		val request = server.takeRequest()

		assertEquals(
			"$serverUrl/enhetsregisteret/api/oppdateringer/enheter?oppdateringsid=10&size=100",
			request.requestUrl.toString()
		)

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
		val responseBody = ResourceUtils.getResourceAsText("/client/hent-underenhet-oppdateringer-response.json")

		server.enqueue(MockResponse().setBody(responseBody))

		val oppdateringer = client.hentUnderenhetOppdateringer(10, 100)

		val request = server.takeRequest()

		assertEquals(
			"$serverUrl/enhetsregisteret/api/oppdateringer/underenheter?oppdateringsid=10&size=100",
			request.requestUrl.toString()
		)

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
