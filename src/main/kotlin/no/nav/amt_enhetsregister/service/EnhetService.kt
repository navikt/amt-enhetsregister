package no.nav.amt_enhetsregister.service

import no.nav.amt_enhetsregister.repository.EnhetRepository
import no.nav.amt_enhetsregister.repository.type.Enhet
import org.springframework.stereotype.Service

@Service
class EnhetService(private val enhetRepository: EnhetRepository) {

	fun hentEnhet(organisasjonsnummer: String): Enhet? {
		return enhetRepository.hentEnhet(organisasjonsnummer)
	}

}
