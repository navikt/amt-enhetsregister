package no.nav.amt_enhetsregister.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

@ActiveProfiles("test")
@AutoConfigureJdbc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class RepositoryTestBase {
	@Autowired
	protected lateinit var dataSource: DataSource

	companion object {
		private const val POSTGRES_DOCKER_IMAGE_NAME = "postgres:14-alpine"

		@Suppress("unused")
		private val postgres = PostgreSQLContainer<Nothing>(POSTGRES_DOCKER_IMAGE_NAME).apply {
			this.start()
		}

		@JvmStatic
		@DynamicPropertySource
		@Suppress("unused")
		fun overrideProps(registry: DynamicPropertyRegistry) {
			registry.add("spring.datasource.url", postgres::getJdbcUrl)
			registry.add("spring.datasource.username", postgres::getUsername)
			registry.add("spring.datasource.password", postgres::getPassword)
		}
	}
}

