package no.nav.amt_enhetsregister.controller

import no.nav.amt_enhetsregister.controller.dto.EnhetDto
import no.nav.amt_enhetsregister.controller.dto.tilDto
import no.nav.amt_enhetsregister.service.EnhetService
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/enhet")
class EnhetController(private val enhetService: EnhetService) {

	@Protected
	@GetMapping
	fun hentEnhet(@RequestParam("organisasjonsnummer") organisasjonsnummer: String): EnhetDto {
		return enhetService.hentEnhet(organisasjonsnummer)?.tilDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Fant ikke enhet")
	}

}
