package no.nav.amt_enhetsregister.schedule

import no.nav.common.job.leader_election.LeaderElectionClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OppdaterEnhetSchedule(
	private val leaderElectionClient: LeaderElectionClient,
) {

	// Every even hour at 0 minutes
	@Scheduled(cron = "0 */2 * * *")
	fun oppdaterModerenheterSchedule() {
		if (leaderElectionClient.isLeader) {
//			enhetService.oppdaterAlleEnheterAvType(OppdaterEnhetJobbType.MODERENHET)
		}
	}

	// Every odd hour at 0 minutes
	@Scheduled(cron = "0 1-23/2 * * *")
	fun oppdaterUnderenhetSchedule() {
		if (leaderElectionClient.isLeader) {
//			enhetService.oppdaterAlleEnheterAvType(OppdaterEnhetJobbType.UNDERENHET)
		}
	}

}
