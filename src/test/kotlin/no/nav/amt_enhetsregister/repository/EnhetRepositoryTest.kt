package no.nav.amt_enhetsregister.repository

import no.nav.amt_enhetsregister.repository.type.UpsertEnhetCmd
import no.nav.amt_enhetsregister.test_utils.DatabaseTestUtils
import no.nav.amt_enhetsregister.test_utils.SingletonPostgresContainer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate

class EnhetRepositoryTest {

	private val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var jdbcTemplate: JdbcTemplate

	lateinit var enhetRepository: EnhetRepository

	@BeforeEach
	fun migrate() {
		jdbcTemplate = JdbcTemplate(dataSource)
		enhetRepository = EnhetRepository(jdbcTemplate)
		DatabaseTestUtils.cleanAndInitDatabase(dataSource,"/db/enhet-data.sql")
	}

	@Test
	fun `upsertEnheter() skal lagre nye enheter`() {
		val enheter = listOf(
			UpsertEnhetCmd(
				navn = "Test 4",
				overordnetEnhet = null,
				organisasjonsnummer = "999888777"
			),
			UpsertEnhetCmd(
				navn = "Test 5",
				overordnetEnhet = "555444333",
				organisasjonsnummer = "888777666"
			)
		)

		enhetRepository.upsertEnheter(enheter)

		val testEnhet4 = enhetRepository.hentEnhet("999888777")
		val testEnhet5 = enhetRepository.hentEnhet("888777666")

		assertNotNull(testEnhet4)
		assertNotNull(testEnhet5)
	}

	@Test
	fun `upsertEnheter() skal oppdatere enhet hvis likt organisasjonsnummer`() {
		val enheter = listOf(
			UpsertEnhetCmd(
				navn = "Test 1 - updated",
				overordnetEnhet = null,
				organisasjonsnummer = "123456789"
			),
			UpsertEnhetCmd(
				navn = "Test 2 - updated",
				overordnetEnhet = "555666444",
				organisasjonsnummer = "344556677"
			)
		)

		enhetRepository.upsertEnheter(enheter)

		val testEnhet1 = enhetRepository.hentEnhet("123456789")
		val testEnhet2 = enhetRepository.hentEnhet("344556677")

		assertEquals("123456789", testEnhet1?.organisasjonsnummer)
		assertEquals("Test 1 - updated", testEnhet1?.navn)
		assertNull(testEnhet1?.overordnetEnhet)

		assertEquals("344556677", testEnhet2?.organisasjonsnummer)
		assertEquals("Test 2 - updated", testEnhet2?.navn)
		assertEquals("555666444", testEnhet2?.overordnetEnhet)
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
