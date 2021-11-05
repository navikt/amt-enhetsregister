package no.nav.amt_enhetsregister.utils

import java.time.ZonedDateTime

object DateUtils {

	fun oneDayAgo(): ZonedDateTime {
		return ZonedDateTime.now().minusDays(1)
	}

	fun oneHourAgo(): ZonedDateTime {
		return ZonedDateTime.now().minusHours(1)
	}

}
