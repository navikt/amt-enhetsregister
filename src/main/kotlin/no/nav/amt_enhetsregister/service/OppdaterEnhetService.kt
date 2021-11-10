package no.nav.amt_enhetsregister.service

import no.nav.amt_enhetsregister.client.BronnoysundClient
import no.nav.amt_enhetsregister.client.EnhetPage
import no.nav.amt_enhetsregister.repository.EnhetRepository
import no.nav.amt_enhetsregister.repository.OppdaterEnhetJobbRepository
import no.nav.amt_enhetsregister.repository.type.OppdaterEnhetJobb
import no.nav.amt_enhetsregister.repository.type.OppdaterEnhetJobbStatus
import no.nav.amt_enhetsregister.repository.type.OppdaterEnhetJobbType
import no.nav.amt_enhetsregister.repository.type.UpsertEnhetCmd
import no.nav.amt_enhetsregister.utils.DateUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OppdaterEnhetService(
	private val enhetRepository: EnhetRepository,
	private val oppdaterEnhetJobbRepository: OppdaterEnhetJobbRepository,
	private val bronnoysundClient: BronnoysundClient
) {

	companion object {
		const val OPPDATER_PROGRESJON_PR_ANTALL_PAGE = 10
	}

	private val log = LoggerFactory.getLogger(this::class.java)

	fun oppdaterAlleEnheterAvType(type: OppdaterEnhetJobbType) {
		val sisteJobb = oppdaterEnhetJobbRepository.hentSisteJobb(type)

		// Vent minst 1 time før en pauset jobb blir jobbet videre på
		if (sisteJobb?.status == OppdaterEnhetJobbStatus.PAUSED && sisteJobb.pausedAt!!.isAfter(DateUtils.oneHourAgo())) {
			log.info("Venter med å starte videre på pauset jobb ${sisteJobb.id} av type $type. Jobben ble pauset ${sisteJobb.pausedAt}")
			return
		}

		// Vent minst 1 dag før vi starter neste jobb
		if (sisteJobb?.status == OppdaterEnhetJobbStatus.COMPLETED && sisteJobb.finishedAt!!.isAfter(DateUtils.oneDayAgo())) {
			log.info("Venter med å starte ny jobb av type $type. Forrige jobb ble ferdig ${sisteJobb.finishedAt}")
			return
		}

		val jobb: OppdaterEnhetJobb = sisteJobb ?: oppdaterEnhetJobbRepository.startJobb(type)

		log.info("Starter på utføring av jobb ${jobb.id}. currentPage=${jobb.currentPage} totalPages=${jobb.totalPages} pageSize=${jobb.pageSize}")

		utfor(jobb)
	}

	private fun utfor(jobb: OppdaterEnhetJobb) {
		var pageCounter = jobb.currentPage
		var totalPages: Int

		try {
			do {
				val enheterPage = hentEnhetPage(jobb.type, pageCounter, jobb.pageSize)
				val page = enheterPage.page

				log.info("Hentet enheter... currentPage=${page.number} size=${enheterPage.enheter.size}")

				val enheter = enheterPage.enheter.map {
					UpsertEnhetCmd(
						organisasjonsnummer = it.organisasjonsnummer,
						navn = it.navn,
						overordnetEnhet = it.overordnetEnhet
					)
				}

				enhetRepository.upsertEnheter(enheter)

				if (enheterPage.page.totalPages % OPPDATER_PROGRESJON_PR_ANTALL_PAGE == 0) {
					log.info("Oppdaterer progresjon... currentPage=${page.number} totalPages=${page.totalPages}")

					oppdaterEnhetJobbRepository.oppdaterProgresjon(
						jobbId = jobb.id,
						currentPage = page.number,
						totalPages = page.totalPages
					)
				}

				totalPages = page.totalPages
				pageCounter++
			} while (pageCounter < totalPages)

			oppdaterEnhetJobbRepository.fullforJobb(jobb.id)
			log.info("Oppdatering av enheter med type ${jobb.type} er fullført")
		} catch (exception: Exception) {
			log.error("Feil under oppdatering av enheter. pageCounter=$pageCounter", exception)
			oppdaterEnhetJobbRepository.markerJobbPauset(jobb.id)
		}
	}

	private fun hentEnhetPage(jobbType: OppdaterEnhetJobbType, page: Int, size: Int): HentEnhetPage {
		when (jobbType) {
			OppdaterEnhetJobbType.MODERENHET -> {
				val moderenhetPage = bronnoysundClient.hentModerenheterPage(page, size)

				return HentEnhetPage(
					enheter = moderenhetPage.moderenheter.map { HentEnhetPage.Enhet(
						organisasjonsnummer = it.organisasjonsnummer,
						navn = it.navn
					) },
					page = moderenhetPage.page
				)
			}
			OppdaterEnhetJobbType.UNDERENHET -> {
				val underEnhetPage = bronnoysundClient.hentUnderenheterPage(page, size)

				return HentEnhetPage(
					enheter = underEnhetPage.underenheter.map { HentEnhetPage.Enhet(
						organisasjonsnummer = it.organisasjonsnummer,
						navn = it.navn,
						overordnetEnhet = it.overordnetEnhet
					) },
					page = underEnhetPage.page
				)
			}
			else -> {
				throw IllegalArgumentException("Ugyldig jobb type $jobbType")
			}
		}
	}

	private data class HentEnhetPage(
		val enheter: List<Enhet>,
		val page: EnhetPage
	) {
		data class Enhet(
			val organisasjonsnummer: String,
			val navn: String,
			val overordnetEnhet: String? = null
		)
	}

}
