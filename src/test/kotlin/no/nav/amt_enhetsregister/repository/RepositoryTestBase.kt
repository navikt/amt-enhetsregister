package no.nav.amt_enhetsregister.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

@ActiveProfiles("test")
@AutoConfigureJdbc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class RepositoryTestBase {
	@Autowired
	protected lateinit var dataSource: DataSource

	companion object {
		private const val POSTGRES_DOCKER_IMAGE_NAME = "postgres:17-alpine"

		@ServiceConnection
		@Suppress("unused")
		private val postgres =
			PostgreSQLContainer<Nothing>(
				DockerImageName
					.parse(POSTGRES_DOCKER_IMAGE_NAME)
					.asCompatibleSubstituteFor("postgres"),
			).apply {
				addEnv("TZ", "Europe/Oslo")
				waitingFor(Wait.forListeningPort())
			}
	}
}
