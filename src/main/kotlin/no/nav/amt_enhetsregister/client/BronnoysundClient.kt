package no.nav.amt_enhetsregister.client

import java.time.ZonedDateTime

interface BronnoysundClient {

	fun hentModerenhetOppdateringer(fraOppdateringId: Int, size: Int): List<EnhetOppdatering>

	fun hentUnderenhetOppdateringer(fraOppdateringId: Int, size: Int): List<EnhetOppdatering>


	fun hentModerenhet(organisasjonsnummer: String): Moderenhet

	fun hentUnderenhet(organisasjonsnummer: String): Underenhet


	fun hentAlleModerenheter(): List<Moderenhet>

	fun hentAlleUnderenheter(): List<Underenhet>

}

enum class EnhetOppdateringType(val type: String) {
	UKJENT("Ukjent"), 			// Ukjent type endring. Ofte fordi endringen har skjedd før endringstype ble innført.
	NY("Ny"), 					// Enheten har blitt lagt til i Enhetsregisteret
	ENDRING("Endring"), 		// Enheten har blitt endret i Enhetsregisteret
	SLETTING("Sletting"), 		// Enheten har blitt slettet fra Enhetsregisteret
	FJERNET("Fjernet"), 		// Enheten har blitt fjernet fra Åpne Data. Eventuelle kopier skal også fjerne enheten.
}

data class EnhetOppdatering(
	val oppdateringId: Int,
	val dato: ZonedDateTime,
	val organisasjonsnummer: String,
	val endringstype: EnhetOppdateringType,
)

data class Moderenhet(
	val organisasjonsnummer: String,
	val navn: String,
	val slettedato: String?,
)

data class Underenhet(
	val organisasjonsnummer: String,
	val navn: String,
	val slettedato: String?,
	val overordnetEnhet: String?,
)

