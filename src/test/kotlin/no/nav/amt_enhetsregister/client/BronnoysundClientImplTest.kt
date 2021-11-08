package no.nav.amt_enhetsregister.client

import no.nav.amt_enhetsregister.utils.ResourceUtils.getResourceAsText
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals


class BronnoysundClientImplTest {

	@Test
	fun `hentModerenheterPage skal lage riktig request og parse response`() {
		val server = MockWebServer()
		val serverUrl = server.url("").toString().dropLast(1) // Removes trailing "/"

		val client = BronnoysundClientImpl(
			bronnoysundUrl = serverUrl
		)

		val responseBody = getResourceAsText("/client/hent-moderenheter-page-response.json")

		server.enqueue(MockResponse().setBody(responseBody))

		val enhetPage = client.hentModerenheterPage(4, 2)

		val request = server.takeRequest()

		assertEquals("$serverUrl/enhetsregisteret/api/enheter?page=4&size=2", request.requestUrl.toString())

		assertEquals(2, enhetPage.moderenheter.size)

		assertEquals(150850, enhetPage.page.totalPages)
		assertEquals(1055949, enhetPage.page.totalElements)
		assertEquals(2, enhetPage.page.size)
		assertEquals(4, enhetPage.page.number)

		assertEquals("111222333", enhetPage.moderenheter[0].organisasjonsnummer)
		assertEquals("Moderenhet 1", enhetPage.moderenheter[0].navn)

		assertEquals("222333444", enhetPage.moderenheter[1].organisasjonsnummer)
		assertEquals("Moderenhet 2", enhetPage.moderenheter[1].navn)
	}

	@Test
	fun `hentUnderenheterPage skal lage riktig request og parse response`() {
		val server = MockWebServer()
		val serverUrl = server.url("").toString().dropLast(1) // Removes trailing "/"

		val client = BronnoysundClientImpl(
			bronnoysundUrl = serverUrl
		)

		val responseBody = getResourceAsText("/client/hent-underenheter-page-response.json")

		server.enqueue(MockResponse().setBody(responseBody))

		val enhetPage = client.hentUnderenheterPage(1, 2)

		val request = server.takeRequest()

		assertEquals("$serverUrl/enhetsregisteret/api/underenheter?page=1&size=2", request.requestUrl.toString())

		assertEquals(2, enhetPage.underenheter.size)

		assertEquals(108127, enhetPage.page.totalPages)
		assertEquals(756888, enhetPage.page.totalElements)
		assertEquals(2, enhetPage.page.size)
		assertEquals(1, enhetPage.page.number)

		assertEquals("54307983", enhetPage.underenheter[0].organisasjonsnummer)
		assertEquals("6432896", enhetPage.underenheter[0].overordnetEnhet)
		assertEquals("Underenhet 1", enhetPage.underenheter[0].navn)

		assertEquals("999666555", enhetPage.underenheter[1].organisasjonsnummer)
		assertEquals("444555666", enhetPage.underenheter[1].overordnetEnhet)
		assertEquals("Underenhet 2", enhetPage.underenheter[1].navn)
	}

}
