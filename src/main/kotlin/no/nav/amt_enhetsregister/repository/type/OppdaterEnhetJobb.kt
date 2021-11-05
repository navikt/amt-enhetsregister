package no.nav.amt_enhetsregister.repository.type

import java.time.ZonedDateTime

data class OppdaterEnhetJobb(
	val id: Int,
	val currentPage: Int,
	val pageSize: Int,
	val totalPages: Int,
	val type: OppdaterEnhetJobbType,
	val status: OppdaterEnhetJobbStatus,
	val finishedAt: ZonedDateTime?,
	val pausedAt: ZonedDateTime?,
)
