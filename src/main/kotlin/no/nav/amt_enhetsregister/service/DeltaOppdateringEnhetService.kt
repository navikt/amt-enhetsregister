package no.nav.amt_enhetsregister.service

import no.nav.amt_enhetsregister.client.BronnoysundClient
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
		const val OPPDATERINGER_SIZE = 100
	}

	private val oppdateringTyperSomSkalSkrivesTilDb = listOf(
		EnhetOppdateringType.ENDRING,
		EnhetOppdateringType.NY,
		EnhetOppdateringType.UKJENT
	)

	private val log = LoggerFactory.getLogger(this::class.java)

	fun deltaOppdaterModerenheter() {
		val progresjon = deltaOppdateringRepository.hentOppdateringProgresjon(EnhetType.MODERENHET)

		log.info("Starter delta oppdatering av moderenheter fra oppdatering_id=${progresjon.oppdateringId}")

		val oppdateringer = bronnoysundClient.hentModerenhetOppdateringer(progresjon.oppdateringId, OPPDATERINGER_SIZE)

		log.info("Antall oppdateringer: ${oppdateringer.size}")

		val upserts = oppdateringer
			.filter { oppdateringTyperSomSkalSkrivesTilDb.contains(it.endringstype) }
			. map {
				val moderenhet = bronnoysundClient.hentModerenhet(it.organisasjonsnummer)

				return@map UpsertEnhetCmd(
					organisasjonsnummer = moderenhet.organisasjonsnummer,
					navn = moderenhet.navn
				)
			}

		enhetRepository.upsertEnheter(upserts)

		val sisteOppdateringId = oppdateringer.maxOf { it.oppdateringId }

		// Legger til +1 på oppdatering id slik at vi ikke henter den siste enheten i neste bolk
		deltaOppdateringRepository.oppdaterProgresjon(EnhetType.MODERENHET, sisteOppdateringId + 1)

		log.info("Fullførte delta oppdatering av moderenheter, sisteOppdateringId=$sisteOppdateringId")
	}

	fun deltaOppdaterUnderenheter() {
		val progresjon = deltaOppdateringRepository.hentOppdateringProgresjon(EnhetType.UNDERENHET)

		log.info("Starter delta oppdatering av underenheter fra oppdatering_id=${progresjon.oppdateringId}")

		val oppdateringer = bronnoysundClient.hentUnderenhetOppdateringer(progresjon.oppdateringId, OPPDATERINGER_SIZE)

		log.info("Antall oppdateringer: ${oppdateringer.size}")

		val upserts = oppdateringer
			.filter { oppdateringTyperSomSkalSkrivesTilDb.contains(it.endringstype) }
			. map {
				val underenhet = bronnoysundClient.hentUnderenhet(it.organisasjonsnummer)

				return@map UpsertEnhetCmd(
					organisasjonsnummer = underenhet.organisasjonsnummer,
					navn = underenhet.navn,
					overordnetEnhet = underenhet.overordnetEnhet
				)
			}

		enhetRepository.upsertEnheter(upserts)

		val sisteOppdateringId = oppdateringer.maxOf { it.oppdateringId }

		// Legger til +1 på oppdatering id slik at vi ikke henter den siste enheten i neste bolk
		deltaOppdateringRepository.oppdaterProgresjon(EnhetType.UNDERENHET, sisteOppdateringId + 1)

		log.info("Fullførte delta oppdatering av underenheter, sisteOppdateringId=$sisteOppdateringId")
	}

}
