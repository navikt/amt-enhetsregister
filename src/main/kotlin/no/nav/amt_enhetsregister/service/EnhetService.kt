package no.nav.amt_enhetsregister.service

import no.nav.amt_enhetsregister.kafka.VirksomhetV1Dto
import no.nav.amt_enhetsregister.repository.EnhetRepository
import no.nav.amt_enhetsregister.repository.type.UpsertEnhetCmd
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class EnhetService(
	private val enhetRepository: EnhetRepository,
	private val kafkaProducerService: KafkaProducerService,
	private val transactionTemplate: TransactionTemplate,
	) {

	fun hentEnhet(organisasjonsnummer: String): EnhetMedOverordnetEnhet? {
		val enhet = enhetRepository.hentEnhet(organisasjonsnummer) ?: return null

		val overordnetEnhetNavn = enhet.overordnetEnhet?.let { enhetRepository.hentEnhet(it)?.navn }

		return EnhetMedOverordnetEnhet(
			organisasjonsnummer = enhet.organisasjonsnummer,
			navn = enhet.navn,
			overordnetEnhetOrganisasjonsnummer = enhet.overordnetEnhet,
			overordnetEnhetNavn = overordnetEnhetNavn
		)
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
