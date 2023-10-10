package no.nav.amt_enhetsregister.controller

import no.nav.amt_enhetsregister.controller.dto.EnhetDto
import no.nav.amt_enhetsregister.controller.dto.tilDto
import no.nav.amt_enhetsregister.service.EnhetService
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/enhet")
class EnhetController(private val enhetService: EnhetService) {

	@Protected
	@GetMapping("/{organisasjonsnummer}")
	fun hentEnhet(@PathVariable("organisasjonsnummer") organisasjonsnummer: String): EnhetDto {
		validerOrganisasjonsnummer(organisasjonsnummer)
		return enhetService.hentEnhet(organisasjonsnummer)?.tilDto() ?: throw NoSuchElementException("Fant ikke enhet med organisasjonsnummer: $organisasjonsnummer")
	}

	private fun validerOrganisasjonsnummer(organisasjonsnummer: String) {
		if (organisasjonsnummer.trim().length != 9 || !organisasjonsnummer.trim().matches("""\d{9}""".toRegex())) {
			throw IllegalArgumentException("Ugyldig organisasjonsnummer $organisasjonsnummer")
		}
	}
}
