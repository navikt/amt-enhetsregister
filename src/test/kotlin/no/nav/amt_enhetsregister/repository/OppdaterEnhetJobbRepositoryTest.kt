package no.nav.amt_enhetsregister.repository

import no.nav.amt_enhetsregister.repository.type.EnhetType
import no.nav.amt_enhetsregister.utils.LocalPostgresDatabase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
class OppdaterEnhetJobbRepositoryTest {

	@Container
	val postgresContainer: PostgreSQLContainer<Nothing> = PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

	lateinit var jdbcTemplate: JdbcTemplate

	lateinit var oppdaterEnhetJobbRepository: DeltaOppdateringProgresjonRepository

	@BeforeEach
	fun migrate() {
		val dataSource = LocalPostgresDatabase.createDataSource(postgresContainer)

		LocalPostgresDatabase.cleanAndMigrate(dataSource)

		jdbcTemplate = JdbcTemplate(dataSource)
		oppdaterEnhetJobbRepository = DeltaOppdateringProgresjonRepository(jdbcTemplate)
	}

	@Test
	fun `hentOppdateringProgresjon skal hente oppdatering progresjon`() {
		val progresjon = oppdaterEnhetJobbRepository.hentOppdateringProgresjon(EnhetType.MODERENHET)

		assertEquals(2147483647, progresjon.oppdateringId)
		assertEquals(EnhetType.MODERENHET, progresjon.enhetType)
	}

	@Test
	fun `oppdaterProgresjon skal oppdatere oppdateringsid`() {
		oppdaterEnhetJobbRepository.oppdaterProgresjon(EnhetType.MODERENHET, 5)

		val progresjon = oppdaterEnhetJobbRepository.hentOppdateringProgresjon(EnhetType.MODERENHET)

		assertEquals(5, progresjon.oppdateringId)
	}

}
