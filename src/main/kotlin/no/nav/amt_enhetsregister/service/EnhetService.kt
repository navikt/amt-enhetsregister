package no.nav.amt_enhetsregister.service

import no.nav.amt_enhetsregister.client.BronnoysundClient
import no.nav.amt_enhetsregister.kafka.VirksomhetV1Dto
import no.nav.amt_enhetsregister.repository.EnhetRepository
import no.nav.amt_enhetsregister.repository.type.UpsertEnhetCmd
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service

class EnhetService(
	private val enhetRepository: EnhetRepository,
	private val bronnoysundClient: BronnoysundClient,
	private val kafkaProducerService: KafkaProducerService,
	private val transactionTemplate: TransactionTemplate,
	) {

	private val log = LoggerFactory.getLogger(this::class.java)

	fun hentEnhet(organisasjonsnummer: String): EnhetMedOverordnetEnhet? {
		val enhet = enhetRepository.hentEnhet(organisasjonsnummer) ?: return fallbackTilBronnoysundOgOppdaterEnhetHvisMangler(organisasjonsnummer)

		val overordnetEnhetNavn = enhet.overordnetEnhet?.let { enhetRepository.hentEnhet(it)?.navn }

		return EnhetMedOverordnetEnhet(
			organisasjonsnummer = enhet.organisasjonsnummer,
			navn = enhet.navn,
			overordnetEnhetOrganisasjonsnummer = enhet.overordnetEnhet,
			overordnetEnhetNavn = overordnetEnhetNavn
		)
	}

	fun fallbackTilBronnoysundOgOppdaterEnhetHvisMangler(organisasjonsnummer: String): EnhetMedOverordnetEnhet? {
		val underEnhet = bronnoysundClient.hentUnderenhet(organisasjonsnummer)
		if (underEnhet != null) {
			log.info("Fant manglende underenhet orgnr=${organisasjonsnummer} i brreg : ${underEnhet} ")
			oppdaterEnheter(
				listOf( UpsertEnhet(
				organisasjonsnummer = organisasjonsnummer,
				navn = "${underEnhet.navn} ${underEnhet.slettedato != (null ?: "(slettet)")}",
				overordnetEnhetOrgNr = underEnhet.overordnetEnhet ?: DeltaOppdateringEnhetService.UKJENT_VIRKSOMHET_NR
			)))
			if (underEnhet.overordnetEnhet != null) {
				val moderEnhet = bronnoysundClient.hentModerenhet(underEnhet.overordnetEnhet)
				if (moderEnhet != null) {
					return EnhetMedOverordnetEnhet(
						organisasjonsnummer = organisasjonsnummer,
						navn = underEnhet.navn,
						overordnetEnhetOrganisasjonsnummer = underEnhet.overordnetEnhet,
						overordnetEnhetNavn = moderEnhet.navn
					)
				}
			} else {
				return EnhetMedOverordnetEnhet(
					organisasjonsnummer = organisasjonsnummer,
					navn = underEnhet.navn,
					overordnetEnhetOrganisasjonsnummer = DeltaOppdateringEnhetService.UKJENT_VIRKSOMHET_NR,
					overordnetEnhetNavn = null
				)
			}
		}
		val moderEnhet = bronnoysundClient.hentModerenhet(organisasjonsnummer)
		if (moderEnhet != null) {
			log.info("Fant manglende moderenhet orgnr=${organisasjonsnummer} i brreg : ${moderEnhet} ")

			oppdaterEnheter(
				listOf( UpsertEnhet(
					organisasjonsnummer = organisasjonsnummer,
					navn = "${moderEnhet.navn} ${moderEnhet.slettedato != (null ?: "(slettet)")}",
					overordnetEnhetOrgNr = null
				)))
			return EnhetMedOverordnetEnhet(
				organisasjonsnummer = organisasjonsnummer,
				navn = moderEnhet.navn,
				overordnetEnhetOrganisasjonsnummer = null,
				overordnetEnhetNavn = null
			)
		}
		return null
	}

	fun oppdaterEnheter(upserts: List<UpsertEnhet>) {
		upserts.forEach { upsert ->
			transactionTemplate.executeWithoutResult {
				enhetRepository.upsertEnhet(
					UpsertEnhetCmd(
						organisasjonsnummer = upsert.organisasjonsnummer,
						navn = upsert.navn,
						overordnetEnhet = upsert.overordnetEnhetOrgNr
					)
				)
				kafkaProducerService.publiserVirksomhet(
					VirksomhetV1Dto(
						organisasjonsnummer = upsert.organisasjonsnummer,
						navn = upsert.navn,
						overordnetEnhetOrganisasjonsnummer = upsert.overordnetEnhetOrgNr
					)
				)
			}
		}
	}

	data class UpsertEnhet(
		val organisasjonsnummer: String,
		val navn: String,
		val overordnetEnhetOrgNr: String?,
	)

	data class EnhetMedOverordnetEnhet(
		val organisasjonsnummer: String,
		val navn: String,
		val overordnetEnhetOrganisasjonsnummer: String? = null,
		val overordnetEnhetNavn: String? = null
	)

}
