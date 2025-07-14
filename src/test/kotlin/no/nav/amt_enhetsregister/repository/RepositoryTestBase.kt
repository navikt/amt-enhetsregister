package no.nav.amt_enhetsregister.repository

import no.nav.amt_enhetsregister.test_utils.SingletonPostgresContainer.postgresContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import javax.sql.DataSource

@ActiveProfiles("test")
@AutoConfigureJdbc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class RepositoryTestBase {
	@Autowired
	protected lateinit var dataSource: DataSource

	companion object {
		@ServiceConnection
		@Suppress("unused")
		private val container = postgresContainer
	}
}
