package no.nav.amt_enhetsregister.service

import no.nav.amt_enhetsregister.repository.EnhetRepository
import org.springframework.stereotype.Service

@Service
class EnhetService(private val enhetRepository: EnhetRepository) {

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

	data class EnhetMedOverordnetEnhet(
		val organisasjonsnummer: String,
		val navn: String,
		val overordnetEnhetOrganisasjonsnummer: String? = null,
		val overordnetEnhetNavn: String? = null
	)

}
