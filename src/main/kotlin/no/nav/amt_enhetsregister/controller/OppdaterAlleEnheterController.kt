package no.nav.amt_enhetsregister.controller

import jakarta.servlet.http.HttpServletRequest
import no.nav.amt_enhetsregister.service.OppdaterAlleEnheterService
import no.nav.common.job.JobRunner
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/local/api/oppdater-alle")
class OppdaterAlleEnheterController(private val oppdaterAlleEnheterService: OppdaterAlleEnheterService) {

	@Unprotected
	@PostMapping("/moderenheter")
	fun republiseringModerenheter(request: HttpServletRequest) {
		if (!erRequestFraLocalhost(request.remoteAddr)) {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}

		JobRunner.runAsync("oppdater_alle_moderenheter") { oppdaterAlleEnheterService.oppdaterAlleModerenheter() }
	}

	@Unprotected
	@PostMapping("/underenheter")
	fun republiseringUnderenheter(request: HttpServletRequest) {
		if (!erRequestFraLocalhost(request.remoteAddr)) {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
		}

		JobRunner.runAsync("oppdater_alle_underenheter") { oppdaterAlleEnheterService.oppdaterAlleUnderenheter() }
	}

	private fun erRequestFraLocalhost(remoteAddr: String): Boolean {
		return remoteAddr == "127.0.0.1"
	}

}
