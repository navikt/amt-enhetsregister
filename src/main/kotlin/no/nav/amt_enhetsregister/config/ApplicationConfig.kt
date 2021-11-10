package no.nav.amt_enhetsregister.config

import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import no.nav.amt_enhetsregister.client.BronnoysundClient
import no.nav.amt_enhetsregister.client.BronnoysundClientImpl
import no.nav.common.job.leader_election.LeaderElectionClient
import no.nav.common.job.leader_election.ShedLockLeaderElectionClient
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
//@EnableScheduling
@EnableJwtTokenValidation
class ApplicationConfig {

	@Bean
	fun leaderElectionClient(jdbcTemplate: JdbcTemplate): LeaderElectionClient {
		return ShedLockLeaderElectionClient(JdbcTemplateLockProvider(jdbcTemplate))
	}

	@Profile("default")
	@Bean
	fun bronnoysundClient(): BronnoysundClient {
		return BronnoysundClientImpl()
	}

}
