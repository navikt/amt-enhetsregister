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
		const val UKJENT_VIRKSOMHET_NR = "999999999"
	}

	private val oppdateringTyperSomSkalSkrivesTilDb = listOf(
		EnhetOppdateringType.ENDRING,
		EnhetOppdateringType.NY,
		EnhetOppdateringType.UKJENT
	)

	private val log = LoggerFactory.getLogger(this::class.java)

	fun deltaOppdaterModerenheter() {
		deltaOppdaterEnhet(enhetType = EnhetType.MODERENHET) {
			val moderenhet = bronnoysundClient.hentModerenhet(it.organisasjonsnummer)

			if (moderenhet == null || moderenhet.slettedato != null) {
				log.info("Modereneht orgnr=${it.organisasjonsnummer} er slettet fra brreg")

				return@deltaOppdaterEnhet UpsertEnhetCmd(
					organisasjonsnummer = it.organisasjonsnummer,
					navn = "Slettet virksomhet",
				)
			}

			UpsertEnhetCmd(
				organisasjonsnummer = moderenhet.organisasjonsnummer,
				navn = moderenhet.navn,
			)
		}
	}

	fun deltaOppdaterUnderenheter() {
		deltaOppdaterEnhet(enhetType = EnhetType.UNDERENHET) {
			val underenhet = bronnoysundClient.hentUnderenhet(it.organisasjonsnummer)

			if (underenhet == null || underenhet.slettedato != null) {
				log.info("Underenhet orgnr=${it.organisasjonsnummer} er slettet fra brreg")

				return@deltaOppdaterEnhet UpsertEnhetCmd(
					organisasjonsnummer = it.organisasjonsnummer,
					navn = "Slettet virksomhet",
					overordnetEnhet = UKJENT_VIRKSOMHET_NR
				)
			}

			if (underenhet.overordnetEnhet == null) {
				log.warn("Underenhet orgnr=${underenhet.organisasjonsnummer} mangler overordnet enhet. oppdatering_id=${it.oppdateringId}")

				return@deltaOppdaterEnhet UpsertEnhetCmd(
					organisasjonsnummer = it.organisasjonsnummer,
					navn = underenhet.navn,
					overordnetEnhet = UKJENT_VIRKSOMHET_NR
				)
			}

			UpsertEnhetCmd(
				organisasjonsnummer = underenhet.organisasjonsnummer,
				navn = underenhet.navn,
				overordnetEnhet = underenhet.overordnetEnhet
			)
		}
	}

	private fun deltaOppdaterEnhet(enhetType: EnhetType, mapper: (oppdatering: EnhetOppdatering) -> UpsertEnhetCmd?) {
		val progresjon = deltaOppdateringRepository.hentOppdateringProgresjon(enhetType)

		log.info("Starter delta oppdatering av $enhetType fra oppdatering_id=${progresjon.oppdateringId}")

		val oppdateringer =
			if (enhetType == EnhetType.MODERENHET)
				bronnoysundClient.hentModerenhetOppdateringer(progresjon.oppdateringId, OPPDATERINGER_SIZE)
			else
				bronnoysundClient.hentUnderenhetOppdateringer(progresjon.oppdateringId, OPPDATERINGER_SIZE)

		log.info("Antall oppdateringer: ${oppdateringer.size}. Har gjenst??ende oppdateringer: ${oppdateringer.size == OPPDATERINGER_SIZE}")

		if (oppdateringer.isEmpty()) {
			log.info("Ingen oppdateringer for $enhetType")
			return
		}

		val upserts = oppdateringer
			.filter { oppdateringTyperSomSkalSkrivesTilDb.contains(it.endringstype) }
			.mapNotNull { mapper.invoke(it) }

		enhetRepository.upsertEnheter(upserts)

		val sisteOppdateringId = oppdateringer.maxOf { it.oppdateringId }

		// Legger til +1 p?? oppdatering id slik at vi ikke henter den siste enheten i neste bolk
		deltaOppdateringRepository.oppdaterProgresjon(enhetType, sisteOppdateringId + 1)

		log.info("Fullf??rte delta oppdatering av $enhetType, sisteOppdateringId=$sisteOppdateringId")
	}

}


