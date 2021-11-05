package no.nav.amt_enhetsregister.service

import no.nav.amt_enhetsregister.client.BronnoysundClient
import no.nav.amt_enhetsregister.repository.EnhetRepository
import no.nav.amt_enhetsregister.repository.OppdaterEnhetJobbRepository
import no.nav.amt_enhetsregister.repository.type.*
import no.nav.amt_enhetsregister.utils.DateUtils.oneDayAgo
import no.nav.amt_enhetsregister.utils.DateUtils.oneHourAgo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EnhetService(
	private val enhetRepository: EnhetRepository,
	private val oppdaterEnhetJobbRepository: OppdaterEnhetJobbRepository,
	private val bronnoysundClient: BronnoysundClient
	) {

	private val log = LoggerFactory.getLogger(this::class.java)

	companion object {
		const val ENHET_PAGE_SIZE = 5000
	}

	fun hentEnhet(organisasjonsnummer: String): Enhet? {
		return enhetRepository.hentEnhet(organisasjonsnummer)
	}

	fun oppdaterAlleEnheterAvType(type: OppdaterEnhetJobbType) {
		val sisteJobb = oppdaterEnhetJobbRepository.hentSisteJobb(type)

		// Vent minst 1 time før en pauset jobb blir jobbet videre på
		if (sisteJobb?.status == OppdaterEnhetJobbStatus.PAUSED && sisteJobb.pausedAt!!.isAfter(oneHourAgo())) {
			return
		}

		// Vent minst 1 dag før vi starter neste jobb
		if (sisteJobb?.status == OppdaterEnhetJobbStatus.COMPLETED && sisteJobb.finishedAt!!.isAfter(oneDayAgo())) {
			return
		}

		val jobb: OppdaterEnhetJobb = sisteJobb ?: oppdaterEnhetJobbRepository.startJobb(type)

		jobbVidereMedOppdaterModerenheterJobb(jobb)
	}

	fun jobbVidereMedOppdaterModerenheterJobb(jobb: OppdaterEnhetJobb) {
		var pageCounter = jobb.currentPage
		var totalPages: Int

		try {
			do {
				val enheterPage = bronnoysundClient.hentModerenheterPage(pageCounter, ENHET_PAGE_SIZE)
				val page = enheterPage.page

				val enheter = enheterPage.enheter.map {
					UpsertEnhetCmd(
						organisasjonsnummer = it.organisasjonsnummer,
						navn = it.navn,
					)
				}

				enhetRepository.upsertEnheter(enheter)

				if (enheterPage.page.totalPages % 10 == 0) {
					oppdaterEnhetJobbRepository.oppdaterProgresjon(
						jobbId = jobb.id,
						currentPage = page.number,
						pageSize = page.size,
						totalPages = page.totalPages
					)
				}

				totalPages = page.totalPages
				pageCounter++
			} while (pageCounter < totalPages)

			oppdaterEnhetJobbRepository.fullforJobb(jobb.id)
		} catch (exception: Exception) {
			log.error("Feil under oppdatering av enheter. pageCounter=$pageCounter", exception)
			oppdaterEnhetJobbRepository.markerJobbPauset(jobb.id)
		}
	}

}
