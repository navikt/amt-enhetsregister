package no.nav.amt_enhetsregister.repository

import no.nav.amt_enhetsregister.repository.type.UpsertEnhetCmd
import no.nav.amt_enhetsregister.test_utils.DatabaseTestUtils
import no.nav.amt_enhetsregister.test_utils.SingletonPostgresContainer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class EnhetRepositoryTest {

	private val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var enhetRepository: EnhetRepository

	@BeforeEach
	fun migrate() {
		val namedJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
		enhetRepository = EnhetRepository(namedJdbcTemplate)
		DatabaseTestUtils.cleanAndInitDatabase(dataSource,"/db/enhet-data.sql")
	}

	@Test
	fun `upsertEnhet() skal lagre ny enhet`() {
		val enhet = UpsertEnhetCmd(
				navn = "Test 4",
				overordnetEnhet = null,
				organisasjonsnummer = "999888777"
			)

		enhetRepository.upsertEnhet(enhet)

		val testEnhet4 = enhetRepository.hentEnhet("999888777")

		assertNotNull(testEnhet4)
	}

	@Test
	fun `upsertEnhet() skal oppdatere enhet hvis likt organisasjonsnummer`() {
		val enhet = UpsertEnhetCmd(
				navn = "Test 1 - updated",
				overordnetEnhet = null,
				organisasjonsnummer = "123456789"
			)

		enhetRepository.upsertEnhet(enhet)

		val testEnhet1 = enhetRepository.hentEnhet("123456789")

		assertEquals("123456789", testEnhet1?.organisasjonsnummer)
		assertEquals("Test 1 - updated", testEnhet1?.navn)
		assertNull(testEnhet1?.overordnetEnhet)

	}

	@Test
	fun `hentEnhet() skal hente enheter`() {
		val testEnhet1 = enhetRepository.hentEnhet("123456789")
		val testEnhet2 = enhetRepository.hentEnhet("344556677")

		assertEquals("123456789", testEnhet1?.organisasjonsnummer)
		assertEquals("Test 1", testEnhet1?.navn)
		assertEquals("999999999", testEnhet1?.overordnetEnhet)

		assertEquals("344556677", testEnhet2?.organisasjonsnummer)
		assertEquals("Test 2", testEnhet2?.navn)
		assertNull(testEnhet2?.overordnetEnhet)
	}

}
