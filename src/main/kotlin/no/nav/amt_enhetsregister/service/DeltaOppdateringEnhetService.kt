package no.nav.amt_enhetsregister.service

import no.nav.amt_enhetsregister.client.BronnoysundClient
import no.nav.amt_enhetsregister.client.EnhetOppdatering
import no.nav.amt_enhetsregister.client.EnhetOppdateringType
import no.nav.amt_enhetsregister.repository.DeltaOppdateringProgresjonRepository
import no.nav.amt_enhetsregister.repository.EnhetRepository
import no.nav.amt_enhetsregister.repository.type.EnhetType
import no.nav.amt_enhetsregister.repository.type.UpsertEnhetCmd
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeltaOppdateringEnhetService(
	private val enhetRepository: EnhetRepository,
	private val deltaOppdateringRepository: DeltaOppdateringProgresjonRepository,
	private val bronnoysundClient: BronnoysundClient
) {

	companion object {
		const val OPPDATERINGER_SIZE = 500
	}

	private val oppdateringTyperSomSkalSkrivesTilDb = listOf(
		EnhetOppdateringType.ENDRING,
		EnhetOppdateringType.NY,
		EnhetOppdateringType.UKJENT
	)

	private val log = LoggerFactory.getLogger(this::class.java)

	fun deltaOppdaterModerenheter() {
		deltaOppdaterEnhet(enhetType = EnhetType.MODERENHET) {
			val underenhet = bronnoysundClient.hentModerenhet(it.organisasjonsnummer)

			UpsertEnhetCmd(
				organisasjonsnummer = underenhet.organisasjonsnummer,
				navn = underenhet.navn,
			)
		}
	}

	fun deltaOppdaterUnderenheter() {
		deltaOppdaterEnhet(enhetType = EnhetType.UNDERENHET) {
			val underenhet = bronnoysundClient.hentUnderenhet(it.organisasjonsnummer)

			UpsertEnhetCmd(
				organisasjonsnummer = underenhet.organisasjonsnummer,
				navn = underenhet.navn,
				overordnetEnhet = underenhet.overordnetEnhet
			)
		}
	}

	private fun deltaOppdaterEnhet(enhetType: EnhetType, mapper: (oppdatering: EnhetOppdatering) -> UpsertEnhetCmd) {
		val progresjon = deltaOppdateringRepository.hentOppdateringProgresjon(enhetType)

		log.info("Starter delta oppdatering av $enhetType fra oppdatering_id=${progresjon.oppdateringId}")

		val oppdateringer = bronnoysundClient.hentUnderenhetOppdateringer(progresjon.oppdateringId, OPPDATERINGER_SIZE)

		log.info("Antall oppdateringer: ${oppdateringer.size}. Har gjenstående oppdateringer: ${oppdateringer.size < OPPDATERINGER_SIZE}")

		if (oppdateringer.isEmpty()) {
			log.info("Ingen oppdateringer for $enhetType")
			return
		}

		val upserts = oppdateringer
			.filter { oppdateringTyperSomSkalSkrivesTilDb.contains(it.endringstype) }
			.map { mapper.invoke(it) }

		enhetRepository.upsertEnheter(upserts)

		val sisteOppdateringId = oppdateringer.maxOf { it.oppdateringId }

		// Legger til +1 på oppdatering id slik at vi ikke henter den siste enheten i neste bolk
		deltaOppdateringRepository.oppdaterProgresjon(enhetType, sisteOppdateringId + 1)

		log.info("Fullførte delta oppdatering av $enhetType, sisteOppdateringId=$sisteOppdateringId")
	}

}


