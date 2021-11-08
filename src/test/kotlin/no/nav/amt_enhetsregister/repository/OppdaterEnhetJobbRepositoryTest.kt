package no.nav.amt_enhetsregister.repository

import no.nav.amt_enhetsregister.repository.type.OppdaterEnhetJobbStatus
import no.nav.amt_enhetsregister.repository.type.OppdaterEnhetJobbType
import no.nav.amt_enhetsregister.utils.LocalPostgresDatabase
import no.nav.amt_enhetsregister.utils.ResourceUtils.getResourceAsText
import org.junit.jupiter.api.Assertions.*
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

	lateinit var oppdaterEnhetJobbRepository: OppdaterEnhetJobbRepository

	@BeforeEach
	fun migrate() {
		val dataSource = LocalPostgresDatabase.createDataSource(postgresContainer)

		LocalPostgresDatabase.cleanAndMigrate(dataSource)

		jdbcTemplate = JdbcTemplate(dataSource)
		oppdaterEnhetJobbRepository = OppdaterEnhetJobbRepository(jdbcTemplate)

		jdbcTemplate.update(getResourceAsText("/db/oppdater-enhet-jobb.sql"))
	}

	@Test
	fun `startJobb skal lage ny jobb`() {
		val jobb = oppdaterEnhetJobbRepository.startJobb(OppdaterEnhetJobbType.MODERENHET)

		assertEquals(5, jobb.id)
		assertEquals(OppdaterEnhetJobbType.MODERENHET, jobb.type)
		assertEquals(0, jobb.currentPage)
		assertEquals(2500, jobb.pageSize)
		assertEquals(0, jobb.totalPages)
		assertEquals(OppdaterEnhetJobbStatus.IN_PROGRESS, jobb.status)
	}

	@Test
	fun `hentJobb skal hente jobb`() {
		val jobb = oppdaterEnhetJobbRepository.hentJobb(4)

		assertEquals(4, jobb.id)
		assertEquals(OppdaterEnhetJobbType.MODERENHET, jobb.type)
		assertEquals(50000, jobb.currentPage)
		assertEquals(10_000, jobb.pageSize)
		assertEquals(50000, jobb.totalPages)
		assertEquals(OppdaterEnhetJobbStatus.COMPLETED, jobb.status)
	}

	@Test
	fun `oppdaterProgresjon skal oppdatere jobb`() {
		oppdaterEnhetJobbRepository.oppdaterProgresjon(
			jobbId = 2,
			currentPage = 100,
			totalPages = 50000
		)

		val jobb = oppdaterEnhetJobbRepository.hentJobb(2)

		assertEquals(100, jobb.currentPage)
		assertEquals(50000, jobb.totalPages)
		assertEquals(OppdaterEnhetJobbStatus.IN_PROGRESS, jobb.status)
	}

	@Test
	fun `markerJobbSomPauset skal sette jobb status til pauset`() {
		oppdaterEnhetJobbRepository.markerJobbPauset(2)

		val jobb = oppdaterEnhetJobbRepository.hentJobb(2)

		assertNotNull(jobb.pausedAt)
		assertEquals(OppdaterEnhetJobbStatus.PAUSED, jobb.status)
	}

	@Test
	fun `fullforJobb skal sette jobb status til ferdig`() {
		oppdaterEnhetJobbRepository.fullforJobb(2)

		val jobb = oppdaterEnhetJobbRepository.hentJobb(2)

		assertNotNull(jobb.finishedAt)
		assertEquals(OppdaterEnhetJobbStatus.COMPLETED, jobb.status)
	}

	@Test
	fun `hentSisteJobb skal hente siste jobb`() {
		val sisteJobb = oppdaterEnhetJobbRepository.hentSisteJobb(OppdaterEnhetJobbType.MODERENHET)

		assertEquals(4, sisteJobb?.id)
	}

	@Test
	fun `hentSisteJobb skal returnere null hvis ingen jobber finnes`() {
		jdbcTemplate.update("DELETE FROM oppdater_enhet_jobb")

		val sisteJobb = oppdaterEnhetJobbRepository.hentSisteJobb(OppdaterEnhetJobbType.MODERENHET)

		assertNull(sisteJobb)
	}

}
