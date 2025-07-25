package no.nav.amt_enhetsregister.client

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt_enhetsregister.client.BronnoysundClient.Companion.mapTilEnhetOppdateringType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.stream.Stream
import java.util.zip.GZIPOutputStream

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

	@AfterEach
	fun teardown() = server.shutdown()

	@Nested
	inner class HentModerenhet {

		@Test
		fun `hentModerenhet skal lage riktig request og parse response`() {
			val responseBody = ClassPathResource("/client/hent-moderenhet-response.json").file.readText()

			server.enqueue(MockResponse().setBody(responseBody))

			val moderenhet = client.hentModerenhet("998877443")

			val request = server.takeRequest()

			assertEquals("$serverUrl/enhetsregisteret/api/enheter/998877443", request.requestUrl.toString())

			assertNotNull(moderenhet)
			assertEquals("998877443", moderenhet?.organisasjonsnummer)
			assertEquals("Moderenhet 1", moderenhet?.navn)
		}

		@Test
		fun `hentModerenhet skal returnere null for status 410 GONE`() {
			server.enqueue(MockResponse().setResponseCode(HttpStatus.GONE.value()))

			assertNull(client.hentModerenhet("12345678"))
		}

		@Test
		fun `hentModerenhet skal returnere null for status 404 NOT_FOUND`() {
			server.enqueue(MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()))

			assertNull(client.hentModerenhet("12345678"))
		}

		@Test
		fun `hentModerenhet skal kaste feil for status 500 INTERNAL_SERVER_ERROR`() {
			server.enqueue(MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()))

			val thrown = assertThrows<RuntimeException> {
				client.hentModerenhet("12345678")
			}

			assertThat(thrown.message).startsWith("Klarte ikke å hente moderenhet for orgnummer 12345678")
		}
	}

	@Nested
	inner class HentUnderenhet {

		@Test
		fun `hentUnderenhet skal lage riktig request og parse response`() {
			val responseBody = ClassPathResource("/client/hent-underenhet-response.json").file.readText()

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
		fun `hentUnderenhet skal returnere null for status 410 GONE`() {
			server.enqueue(MockResponse().setResponseCode(HttpStatus.GONE.value()))

			assertNull(client.hentUnderenhet("12345678"))
		}

		@Test
		fun `hentUnderenhet skal returnere null for status 404 NOT_FOUND`() {
			server.enqueue(MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()))

			assertNull(client.hentUnderenhet("12345678"))
		}

		@Test
		fun `hentUnderenhet skal kaste feil for status 500 INTERNAL_SERVER_ERROR`() {
			server.enqueue(MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()))

			val thrown = assertThrows<RuntimeException> {
				client.hentUnderenhet("12345678")
			}

			assertThat(thrown.message).startsWith("Klarte ikke å hente underenhet for orgnummer 12345678")
		}
	}

	@Nested
	inner class HentModerenhetOppdateringer {
		private val responseBody =
			ClassPathResource("/client/hent-moderenhet-oppdateringer-response.json").file.readText()

		@Test
		fun `hentModerenhetOppdateringer skal kaste feil for status 410 GONE`() {
			server.enqueue(MockResponse().setResponseCode(HttpStatus.GONE.value()))

			val thrown = assertThrows<RuntimeException> {
				client.hentModerenhetOppdateringer(fraOppdateringId = 10, size = 100)
			}

			assertThat(thrown.message).startsWith("Klarte ikke å hente moderenhet oppdateringer.")
		}

		@Test
		fun `hentModerenhetOppdateringer skal returnere tom liste hvis _embedded mangler`() {
			server.enqueue(
				MockResponse().setBody(
					objectMapper.writeValueAsString(HentModerenhetOppdateringerResponse(null))
				)
			)

			val enhetOppdateringer = client.hentModerenhetOppdateringer(fraOppdateringId = 10, size = 100)

			assertEquals(emptyList<EnhetOppdatering>(), enhetOppdateringer)
		}

		@Test
		fun `hentModerenhetOppdateringer skal lage riktig request og parse response`() {
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
	}

	@Nested
	inner class HentUnderenhetOppdateringer {
		private val responseBody =
			ClassPathResource("/client/hent-underenhet-oppdateringer-response.json").file.readText()

		@Test
		fun `hentUnderenhetOppdateringer skal kaste feil for status 410 GONE`() {
			server.enqueue(MockResponse().setResponseCode(HttpStatus.GONE.value()))

			val thrown = assertThrows<RuntimeException> {
				client.hentUnderenhetOppdateringer(fraOppdateringId = 10, size = 100)
			}

			assertThat(thrown.message).startsWith("Klarte ikke å hente underenhet oppdateringer.")
		}

		@Test
		fun `hentUnderenhetOppdateringer skal returnere tom liste hvis _embedded mangler`() {
			server.enqueue(
				MockResponse().setBody(
					objectMapper.writeValueAsString(
						HentUnderenhetOppdateringerResponse(
							null
						)
					)
				)
			)

			val enhetOppdateringer = client.hentUnderenhetOppdateringer(fraOppdateringId = 10, size = 100)

			assertEquals(emptyList<EnhetOppdatering>(), enhetOppdateringer)
		}

		@Test
		fun `hentUnderenhetOppdateringer skal lage riktig request og parse response`() {
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

	@Nested
	inner class HentAlleModerenheter {
		@Test
		fun `hentAlleModerenheter skal kaste feil for status 410 GONE`() {
			server.enqueue(MockResponse().setResponseCode(HttpStatus.GONE.value()))

			val thrown = assertThrows<RuntimeException> {
				client.hentAlleModerenheter()
			}

			assertThat(thrown.message).startsWith("Klarte ikke å laste ned moderenheter.")
		}

		@Test
		fun `hentAlleModerenheter skal returnere liste med moderenheter`() {
			val expected = listOf(Moderenhet("~organisasjonsnummer~", "~navn~", null))
			enqueueByteArray(objectMapper.writeValueAsBytes(expected))

			val moderEnheter = client.hentAlleModerenheter()

			assertEquals(expected, moderEnheter)
		}
	}

	@Nested
	inner class HentAlleUnderenheter {
		@Test
		fun `hentAlleUnderenheter skal kaste feil for status 410 GONE`() {
			server.enqueue(MockResponse().setResponseCode(HttpStatus.GONE.value()))

			val thrown = assertThrows<RuntimeException> {
				client.hentAlleUnderenheter()
			}

			assertThat(thrown.message).startsWith("Klarte ikke å laste ned underenheter.")
		}

		@Test
		fun `hentAlleUnderenheter skal returnere liste med underenheter`() {
			val expected = listOf(Underenhet("~organisasjonsnummer~", "~navn~", null, null))
			enqueueByteArray(objectMapper.writeValueAsBytes(expected))

			val underEnheter = client.hentAlleUnderenheter()

			assertEquals(expected, underEnheter)
		}
	}

	@Nested
	inner class MapTilEnhetOppdateringType {

		@ParameterizedTest(name = "{0} skal returnere {1}")
		@MethodSource("no.nav.amt_enhetsregister.client.BronnoysundClientTest#mapTilEnhetOppdateringTypeSource")
		fun mapTilEnhetOppdateringType(value: String, expectedType: EnhetOppdateringType) {
			assertThat(mapTilEnhetOppdateringType(value)).isEqualTo(expectedType)
		}

		@Test
		fun `mapTilEnhetOppdateringType skal kaste feil ved ukjent type`() {
			val thrown = assertThrows<IllegalArgumentException> {
				mapTilEnhetOppdateringType("~unknown~")
			}

			assertEquals("Ukjent EnhetOppdateringType: ~unknown~", thrown.message)
		}
	}

	private fun enqueueByteArray(bytes: ByteArray) {
		val gzippedBody = ByteArrayOutputStream().use { byteStream ->
			GZIPOutputStream(byteStream).use { it.write(bytes) }
			byteStream.toByteArray()
		}

		server.enqueue(MockResponse().setBody(Buffer().write(gzippedBody)))
	}

	companion object {
		@JvmStatic
		fun mapTilEnhetOppdateringTypeSource(): Stream<Arguments?> = Stream.of<Arguments?>(
			Arguments.of("Ny", EnhetOppdateringType.NY),
			Arguments.of("Ukjent", EnhetOppdateringType.UKJENT),
			Arguments.of("Fjernet", EnhetOppdateringType.FJERNET),
			Arguments.of("Sletting", EnhetOppdateringType.SLETTING),
			Arguments.of("Endring", EnhetOppdateringType.ENDRING),
		)
	}
}
