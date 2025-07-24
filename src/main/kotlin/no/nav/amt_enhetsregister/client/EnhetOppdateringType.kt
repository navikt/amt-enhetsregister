package no.nav.amt_enhetsregister.client

enum class EnhetOppdateringType(val type: String) {
	UKJENT("Ukjent"), 			// Ukjent type endring. Ofte fordi endringen har skjedd før endringstype ble innført.
	NY("Ny"), 					// Enheten har blitt lagt til i Enhetsregisteret
	ENDRING("Endring"), 		// Enheten har blitt endret i Enhetsregisteret
	SLETTING("Sletting"), 		// Enheten har blitt slettet fra Enhetsregisteret
	FJERNET("Fjernet"), 		// Enheten har blitt fjernet fra Åpne Data. Eventuelle kopier skal også fjerne enheten.
}
