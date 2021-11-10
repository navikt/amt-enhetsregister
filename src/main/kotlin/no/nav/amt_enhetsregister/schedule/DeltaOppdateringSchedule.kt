package no.nav.amt_enhetsregister.schedule

import no.nav.amt_enhetsregister.service.DeltaOppdateringEnhetService
import no.nav.common.job.leader_election.LeaderElectionClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DeltaOppdateringSchedule(
	private val leaderElectionClient: LeaderElectionClient,
	private val deltaOppdateringEnhetService: DeltaOppdateringEnhetService
) {

	// Every hour at 0 minutes
	@Scheduled(cron = "0 * * * *")
	fun oppdaterModerenheterSchedule() {
		if (leaderElectionClient.isLeader) {
			deltaOppdateringEnhetService.deltaOppdaterModerenheter()
		}
	}

	// Every hour at 30 minutes
	@Scheduled(cron = "30 * * * *")
	fun oppdaterUnderenhetSchedule() {
		if (leaderElectionClient.isLeader) {
			deltaOppdateringEnhetService.deltaOppdaterUnderenheter()
		}
	}

}
