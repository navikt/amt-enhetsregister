package no.nav.amt_enhetsregister.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt_enhetsregister.client.BronnoysundClient
import no.nav.amt_enhetsregister.client.Moderenhet
import no.nav.amt_enhetsregister.client.Underenhet
import no.nav.amt_enhetsregister.repository.EnhetRepository
import no.nav.amt_enhetsregister.repository.type.Enhet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.time.ZonedDateTime
import java.util.function.Consumer

private const val ORGNR_UNDERENHET = "999000000"

private const val ORGNR_MODERENHET = "777000000"

class EnhetServiceTest {
	val enhetRepository: EnhetRepository = mockk()
	val bronnoysundClient: BronnoysundClient = mockk()
	val kafkaProducerService: KafkaProducerService = mockk()
	val transactionTemplate: TransactionTemplate = mockk()
	val enhetService = EnhetService(enhetRepository, bronnoysundClient, kafkaProducerService, transactionTemplate)

	@BeforeEach
	fun setupMock() {
		// mock TransactionTemplate
		every {
			transactionTemplate.executeWithoutResult(any())
		} answers { invocation ->
			val action = invocation.invocation.args[0] as Consumer<TransactionStatus>
			action.accept(SimpleTransactionStatus())
		}
		// hent underenhet via moderenhet-api gir ikke noe resultat
		every {
			bronnoysundClient.hentModerenhet(ORGNR_UNDERENHET)
		} returns null
		// hent moderenhet via underenhet-api gir ikke noe resultat
		every {
			bronnoysundClient.hentUnderenhet(ORGNR_MODERENHET)
		} returns null
		// Oppdatering av db når vi finner enhet i brreg
		every {
			enhetRepository.upsertEnhet(any())
		} returns Unit
		// kafka melding når db er oppdatert
		every {
			kafkaProducerService.publiserVirksomhet(any())
		} returns Unit

	}

	@Test
	fun hent_underenhet__underenhet_og_moderenhet_finnes_i_db() {
		every {
			enhetRepository.hentEnhet(ORGNR_UNDERENHET)
		} returns Enhet(
			id = 1,
			organisasjonsnummer = ORGNR_UNDERENHET,
			navn = "Enhet1",
			overordnetEnhet = ORGNR_MODERENHET
		)
		every {
			enhetRepository.hentEnhet(ORGNR_MODERENHET)
		} returns Enhet(
			id = 1,
			organisasjonsnummer = ORGNR_MODERENHET,
			navn = "OverEnhet1",
			overordnetEnhet = null
		)

		val enhet = enhetService.hentEnhet(ORGNR_UNDERENHET)
		assertThat(enhet?.navn).isEqualTo("Enhet1")
		assertThat(enhet?.overordnetEnhetNavn).isEqualTo("OverEnhet1")
		assertThat(enhet?.organisasjonsnummer).isEqualTo(ORGNR_UNDERENHET)
		assertThat(enhet?.overordnetEnhetOrganisasjonsnummer).isEqualTo(ORGNR_MODERENHET)

	}

	@Test
	fun hent_underenhet__underenhet_finnes_i_db_men_ikke_moderenhet() {
		every {
			enhetRepository.hentEnhet(ORGNR_UNDERENHET)
		} returns Enhet(
			id = 1,
			organisasjonsnummer = ORGNR_UNDERENHET,
			navn = "Enhet1",
			overordnetEnhet = ORGNR_MODERENHET
		)
		every {
			enhetRepository.hentEnhet(ORGNR_MODERENHET)
		} returns null

		val enhet = enhetService.hentEnhet(ORGNR_UNDERENHET)
		assertThat(enhet?.navn).isEqualTo("Enhet1")
		assertThat(enhet?.overordnetEnhetNavn).isNull()
		assertThat(enhet?.organisasjonsnummer).isEqualTo(ORGNR_UNDERENHET)
		assertThat(enhet?.overordnetEnhetOrganisasjonsnummer).isEqualTo(ORGNR_MODERENHET)
		assertThat(enhet?.overordnetEnhetNavn).isNull()
	}

	@Test
	fun hent_moderenhet_finnes_i_db() {
		every {
			enhetRepository.hentEnhet(ORGNR_MODERENHET)
		} returns Enhet(
			id = 1,
			organisasjonsnummer = ORGNR_MODERENHET,
			navn = "OverEnhet1",
			overordnetEnhet = null
		)
		val enhet = enhetService.hentEnhet(ORGNR_MODERENHET)
		assertThat(enhet?.navn).isEqualTo("OverEnhet1")
		assertThat(enhet?.organisasjonsnummer).isEqualTo(ORGNR_MODERENHET)
		assertThat(enhet?.overordnetEnhetOrganisasjonsnummer).isNull()
		assertThat(enhet?.overordnetEnhetNavn).isNull()
	}

	@Test
	fun hent_underenhet_mangler_i_db_skal_kalle_brreg_klient() {

		// Finner ikke enhet i db
		every {
			enhetRepository.hentEnhet(any())
		} returns null

		every {
			bronnoysundClient.hentUnderenhet(ORGNR_UNDERENHET)
		} returns Underenhet(
			organisasjonsnummer = ORGNR_UNDERENHET,
			navn = "Underenhet1",
			slettedato = null,
			overordnetEnhet = ORGNR_MODERENHET
		)

		every {
			bronnoysundClient.hentModerenhet(ORGNR_MODERENHET)
		} returns Moderenhet(
			organisasjonsnummer = ORGNR_UNDERENHET,
			navn = "Moderenhet1",
			slettedato = null)

		val enhet = enhetService.hentEnhet(ORGNR_UNDERENHET)
		assertThat(enhet?.navn).isEqualTo("Underenhet1")
		assertThat(enhet?.overordnetEnhetNavn).isEqualTo("Moderenhet1")
		assertThat(enhet?.organisasjonsnummer).isEqualTo(ORGNR_UNDERENHET)
		assertThat(enhet?.overordnetEnhetOrganisasjonsnummer).isEqualTo(ORGNR_MODERENHET)

		verify(exactly = 1) {
			bronnoysundClient.hentUnderenhet(ORGNR_UNDERENHET)
			bronnoysundClient.hentModerenhet(ORGNR_MODERENHET)
		}

		// Db oppdatert og kafkamelding publisert etter info hentet fra brreg
		verify(exactly = 2) {
			enhetRepository.upsertEnhet(any())
			kafkaProducerService.publiserVirksomhet(any())
		}
	}

	@Test
	fun `hva skjer naar vi henter slettet enhet`() {
		// Finner ikke enhet i db
		every {
			enhetRepository.hentEnhet(any())
		} returns null

		every {
			bronnoysundClient.hentUnderenhet(ORGNR_UNDERENHET)
		} returns Underenhet(
			organisasjonsnummer = ORGNR_UNDERENHET,
			navn = "Underenhet1",
			slettedato = ZonedDateTime.now().toString(),
			overordnetEnhet = null
		)

		val enhet = enhetService.hentEnhet(ORGNR_UNDERENHET)
		assertThat(enhet?.navn).isEqualTo("Underenhet1 (slettet)")
		assertThat(enhet?.overordnetEnhetNavn).isNull()
		assertThat(enhet?.organisasjonsnummer).isEqualTo(ORGNR_UNDERENHET)
		assertThat(enhet?.overordnetEnhetOrganisasjonsnummer).isNull()

		// Db oppdatert og kafkamelding publisert etter info hentet fra brreg
		verify(exactly = 1) {
			enhetRepository.upsertEnhet(any())
			kafkaProducerService.publiserVirksomhet(any())
		}
	}

	@Test
	fun `hentEnhet - enhet finnes ikke i db, er moderenhet - underhent fra brreg er null, skal hente moderenhet fra brreg`() {
		val moderenhet = Moderenhet(ORGNR_MODERENHET, "Ny Moderenhet", null)
		// Finner ikke enhet i db
		every {
			enhetRepository.hentEnhet(any())
		} returns null
		// Finner ikke underenhet i brreg
		every {
			bronnoysundClient.hentUnderenhet(ORGNR_MODERENHET)
		} returns null
		// Finner moderenhet i brreg
		every {
			bronnoysundClient.hentModerenhet(ORGNR_MODERENHET)
		} returns moderenhet

		val enhet = enhetService.hentEnhet(ORGNR_MODERENHET)

		verify(exactly = 1) { bronnoysundClient.hentUnderenhet(ORGNR_MODERENHET) }
		verify(exactly = 1) { bronnoysundClient.hentModerenhet(ORGNR_MODERENHET) }

		assertThat(enhet?.navn).isEqualTo(moderenhet.navn)
		assertThat(enhet?.organisasjonsnummer).isEqualTo(moderenhet.organisasjonsnummer)
		assertThat(enhet?.overordnetEnhetOrganisasjonsnummer).isNull()
		assertThat(enhet?.overordnetEnhetNavn).isNull()
	}


	@Test
	fun `hva skjer naar enhet ikke finnes noe sted`() {
		// Finner ikke enhet i db
		every {
			enhetRepository.hentEnhet(any())
		} returns null
		// Finner ikke enhet i brreg
		every {
			bronnoysundClient.hentUnderenhet(ORGNR_UNDERENHET)
		} returns null

		val enhet = enhetService.hentEnhet(ORGNR_UNDERENHET)

		verify(exactly = 1) { bronnoysundClient.hentUnderenhet(ORGNR_UNDERENHET) }
		verify(exactly = 1) { bronnoysundClient.hentModerenhet(ORGNR_UNDERENHET) }

		assertThat(enhet).isNull()
	}

}

