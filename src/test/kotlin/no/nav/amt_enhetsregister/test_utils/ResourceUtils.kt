package no.nav.amt_enhetsregister.test_utils

import org.springframework.core.io.ClassPathResource

object ResourceUtils {

	fun getResourceAsText(path: String): String =
		ClassPathResource(path).file.readText()
}
