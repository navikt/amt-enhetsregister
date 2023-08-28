package no.nav.amt_enhetsregister.service
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt_enhetsregister.client.*
import no.nav.amt_enhetsregister.repository.DeltaOppdateringProgresjonRepository
import no.nav.amt_enhetsregister.repository.type.DeltaEnhetOppdateringProgresjon
import no.nav.amt_enhetsregister.repository.type.EnhetType
import no.nav.amt_enhetsregister.service.DeltaOppdateringEnhetService.Companion.UKJENT_VIRKSOMHET_NR
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class DeltaOppdateringEnhetServiceTest {

	val bronnoysundClient: BronnoysundClient = mockk()
	val enhetService: EnhetService = mockk()
	val deltaOppdateringProgresjonRepository: DeltaOppdateringProgresjonRepository = mockk()
	val deltaOppdateringEnhetService = DeltaOppdateringEnhetService(
		enhetService = enhetService,
		deltaOppdateringRepository = deltaOppdateringProgresjonRepository,
		bronnoysundClient = bronnoysundClient
	)

	@BeforeEach
	fun setupMock() {
		val dato = ZonedDateTime.now()

		every {
			deltaOppdateringProgresjonRepository.hentOppdateringProgresjon(EnhetType.MODERENHET)
		} returns DeltaEnhetOppdateringProgresjon(33, EnhetType.MODERENHET, dato)

		every {
			deltaOppdateringProgresjonRepository.hentOppdateringProgresjon(EnhetType.UNDERENHET)
		} returns DeltaEnhetOppdateringProgresjon(33, EnhetType.UNDERENHET, dato)


		every {
			enhetService.oppdaterEnheter(any())
		} returns Unit
		every {
			deltaOppdateringProgresjonRepository.oppdaterProgresjon(any(), any())
		} returns Unit
	}

	// Moderenheter
	@Test
	fun `endret moderenhet skal endres`() {
		every {
			bronnoysundClient.hentModerenhetOppdateringer(any(), any())
		} returns listOf(
			EnhetOppdatering(
				oppdateringId = 33,
				dato = ZonedDateTime.now(),
				organisasjonsnummer = "999000000",
				endringstype = EnhetOppdateringType.ENDRING
			))
		every {
			bronnoysundClient.hentModerenhet("999000000")
		} returns Moderenhet(
			organisasjonsnummer = "999000000",
			navn = "Nytt Orgnavn",
			slettedato = null)
		deltaOppdateringEnhetService.deltaOppdaterModerenheter()
		verify(exactly = 1) {
			enhetService.oppdaterEnheter(listOf(
				EnhetService.UpsertEnhet(
					organisasjonsnummer = "999000000",
					navn = "Nytt Orgnavn",
					overordnetEnhetOrgNr = null
				)
			))
		}
	}

	@Test
	fun `ny moderenhet skal upsertes`() {
		every {
			bronnoysundClient.hentModerenhetOppdateringer(any(), any())
		} returns listOf(
			EnhetOppdatering(
				oppdateringId = 33,
				dato = ZonedDateTime.now(),
				organisasjonsnummer = "999000000",
				endringstype = EnhetOppdateringType.NY
			))
		every {
			bronnoysundClient.hentModerenhet("999000000")
		} returns Moderenhet(
			organisasjonsnummer = "999000000",
			navn = "Orgnavn",
			slettedato = null)
		deltaOppdateringEnhetService.deltaOppdaterModerenheter()
		verify(exactly = 1) {
			enhetService.oppdaterEnheter(listOf(
				EnhetService.UpsertEnhet(
					organisasjonsnummer = "999000000",
					navn = "Orgnavn",
					overordnetEnhetOrgNr = null
				)
			))
		}
	}

	@Test
	fun `ukjent endring av moderenhet skal upsertes`() {
		every {
			bronnoysundClient.hentModerenhetOppdateringer(any(), any())
		} returns listOf(
			EnhetOppdatering(
				oppdateringId = 33,
				dato = ZonedDateTime.now(),
				organisasjonsnummer = "999000000",
				endringstype = EnhetOppdateringType.UKJENT
			))
		every {
			bronnoysundClient.hentModerenhet("999000000")
		} returns Moderenhet(
			organisasjonsnummer = "999000000",
			navn = "Orgnavn",
			slettedato = null)
		deltaOppdateringEnhetService.deltaOppdaterModerenheter()
		verify(exactly = 1) {
			enhetService.oppdaterEnheter(listOf(
				EnhetService.UpsertEnhet(
					organisasjonsnummer = "999000000",
					navn = "Orgnavn",
					overordnetEnhetOrgNr = null
				)
			))
		}
	}

	@Test
	fun `sletting av moderenhet skal gi slettet i navnet `() {
		every {
			bronnoysundClient.hentModerenhetOppdateringer(any(), any())
		} returns listOf(
			EnhetOppdatering(
				oppdateringId = 33,
				dato = ZonedDateTime.now(),
				organisasjonsnummer = "999000000",
				endringstype = EnhetOppdateringType.SLETTING
			))
		every {
			bronnoysundClient.hentModerenhet("999000000")
		} returns Moderenhet(
			organisasjonsnummer = "999000000",
			navn = "Orgnavn",
			slettedato = ZonedDateTime.now().toString())
		deltaOppdateringEnhetService.deltaOppdaterModerenheter()
		verify(exactly = 1) {
			enhetService.oppdaterEnheter(listOf(
				EnhetService.UpsertEnhet(
					organisasjonsnummer = "999000000",
					navn = "Orgnavn (slettet)",
					overordnetEnhetOrgNr = null
				)
			))
		}
	}

	@Test
	fun `fjerning av moderenhet skal gi fjernet navn `() {
		every {
			bronnoysundClient.hentModerenhetOppdateringer(any(), any())
		} returns listOf(
			EnhetOppdatering(
				oppdateringId = 33,
				dato = ZonedDateTime.now(),
				organisasjonsnummer = "999000000",
				endringstype = EnhetOppdateringType.FJERNET
			))
		every {
			bronnoysundClient.hentModerenhet("999000000")
		} returns null
		deltaOppdateringEnhetService.deltaOppdaterModerenheter()
		verify(exactly = 1) {
			enhetService.oppdaterEnheter(listOf(
				EnhetService.UpsertEnhet(
					organisasjonsnummer = "999000000",
					navn = "Fjernet virksomhet",
					overordnetEnhetOrgNr = null
				)
			))
		}
	}

	// Samme for underenheter

	@Test
	fun `endret underenhet skal endres`() {
		every {
			bronnoysundClient.hentUnderenhetOppdateringer(any(), any())
		} returns listOf(
			EnhetOppdatering(
				oppdateringId = 33,
				dato = ZonedDateTime.now(),
				organisasjonsnummer = "899000000",
				endringstype = EnhetOppdateringType.ENDRING
			))
		every {
			bronnoysundClient.hentUnderenhet("899000000")
		} returns Underenhet(
			organisasjonsnummer = "899000000",
			navn = "Nytt Orgnavn",
			slettedato = null,
			overordnetEnhet = "999000000")
		deltaOppdateringEnhetService.deltaOppdaterUnderenheter()
		verify(exactly = 1) {
			enhetService.oppdaterEnheter(listOf(
				EnhetService.UpsertEnhet(
					organisasjonsnummer = "899000000",
					navn = "Nytt Orgnavn",
					overordnetEnhetOrgNr = "999000000"
				)
			))
		}
	}

	@Test
	fun `ny underenhet skal upsertes`() {
		every {
			bronnoysundClient.hentUnderenhetOppdateringer(any(), any())
		} returns listOf(
			EnhetOppdatering(
				oppdateringId = 33,
				dato = ZonedDateTime.now(),
				organisasjonsnummer = "899000000",
				endringstype = EnhetOppdateringType.NY
			))
		every {
			bronnoysundClient.hentUnderenhet("899000000")
		} returns Underenhet(
			organisasjonsnummer = "899000000",
			navn = "Orgnavn",
			slettedato = null,
			overordnetEnhet = "999000000")
		deltaOppdateringEnhetService.deltaOppdaterUnderenheter()
		verify(exactly = 1) {
			enhetService.oppdaterEnheter(listOf(
				EnhetService.UpsertEnhet(
					organisasjonsnummer = "899000000",
					navn = "Orgnavn",
					overordnetEnhetOrgNr = "999000000"
				)
			))
		}
	}

	@Test
	fun `ukjent endring av underenhet skal upsertes`() {
		every {
			bronnoysundClient.hentUnderenhetOppdateringer(any(), any())
		} returns listOf(
			EnhetOppdatering(
				oppdateringId = 33,
				dato = ZonedDateTime.now(),
				organisasjonsnummer = "899000000",
				endringstype = EnhetOppdateringType.UKJENT
			))
		every {
			bronnoysundClient.hentUnderenhet("899000000")
		} returns Underenhet(
			organisasjonsnummer = "899000000",
			navn = "Orgnavn",
			slettedato = null,
			overordnetEnhet = "999000000")
		deltaOppdateringEnhetService.deltaOppdaterUnderenheter()
		verify(exactly = 1) {
			enhetService.oppdaterEnheter(listOf(
				EnhetService.UpsertEnhet(
					organisasjonsnummer = "899000000",
					navn = "Orgnavn",
					overordnetEnhetOrgNr = "999000000"
				)
			))
		}
	}

	@Test
	fun `sletting av underenhet skal gi slettet i navnet `() {
		every {
			bronnoysundClient.hentUnderenhetOppdateringer(any(), any())
		} returns listOf(
			EnhetOppdatering(
				oppdateringId = 33,
				dato = ZonedDateTime.now(),
				organisasjonsnummer = "899000000",
				endringstype = EnhetOppdateringType.SLETTING
			))
		every {
			bronnoysundClient.hentUnderenhet("899000000")
		} returns Underenhet(
			organisasjonsnummer = "899000000",
			navn = "Orgnavn",
			slettedato = ZonedDateTime.now().toString(),
			overordnetEnhet = null)
		deltaOppdateringEnhetService.deltaOppdaterUnderenheter()
		verify(exactly = 1) {
			enhetService.oppdaterEnheter(listOf(
				EnhetService.UpsertEnhet(
					organisasjonsnummer = "899000000",
					navn = "Orgnavn (slettet)",
					overordnetEnhetOrgNr = UKJENT_VIRKSOMHET_NR
				)
			))
		}
	}

	@Test
	fun `fjerning av underenhet skal gi fjernet navn `() {
		every {
			bronnoysundClient.hentUnderenhetOppdateringer(any(), any())
		} returns listOf(
			EnhetOppdatering(
				oppdateringId = 33,
				dato = ZonedDateTime.now(),
				organisasjonsnummer = "899000000",
				endringstype = EnhetOppdateringType.FJERNET
			))
		every {
			bronnoysundClient.hentUnderenhet("899000000")
		} returns null
		deltaOppdateringEnhetService.deltaOppdaterUnderenheter()
		verify(exactly = 1) {
			enhetService.oppdaterEnheter(listOf(
				EnhetService.UpsertEnhet(
					organisasjonsnummer = "899000000",
					navn = "Fjernet virksomhet",
					overordnetEnhetOrgNr = UKJENT_VIRKSOMHET_NR
				)
			))
		}
	}
}
