package no.nav.amt_enhetsregister.repository.type

enum class OppdaterEnhetJobbStatus(val status: String) {
	IN_PROGRESS("IN_PROGRESS"),
	COMPLETED("COMPLETED"),
	PAUSED("PAUSED")
}

