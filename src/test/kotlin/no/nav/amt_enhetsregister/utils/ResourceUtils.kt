package no.nav.amt_enhetsregister.utils

object ResourceUtils {

	fun getResourceAsText(path: String): String {
		return object {}.javaClass.getResource(path).readText()
	}

}
