package no.nav.enhetsregister.repository

import no.nav.enhetsregister.repository.type.Enhet
import no.nav.enhetsregister.repository.type.UpsertEnhetCmd
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class EnhetRepository(
	private val namedJdbcTemplate: NamedParameterJdbcTemplate,
) {

	val rowMapper =
		RowMapper { rs, _ ->
			Enhet(
				id = rs.getInt("id"),
				organisasjonsnummer = rs.getString("organisasjonsnummer"),
				navn = rs.getString("navn"),
				overordnetEnhet = rs.getString("overordnet_enhet")
			)
		}

	fun hentEnhet(organisasjonsnummer: String): Enhet? {
		val sql = """
			SELECT * FROM enhet WHERE organisasjonsnummer = :organisasjonsnummer LIMIT 1
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValue("organisasjonsnummer", organisasjonsnummer)
		return namedJdbcTemplate.query(sql, parameters, rowMapper).firstOrNull()
	}

	fun upsertEnhet(upsertEnhetCmd: UpsertEnhetCmd) {
		val sql = """
			INSERT INTO enhet (organisasjonsnummer, navn, overordnet_enhet) VALUES (:organisasjonsnummer, :navn, :overordnetEnhet)
			ON CONFLICT (organisasjonsnummer)
			DO UPDATE SET navn = EXCLUDED.navn, overordnet_enhet = EXCLUDED.overordnet_enhet, updated_at = CURRENT_TIMESTAMP
		""".trimIndent()

		val parameters = MapSqlParameterSource()
			.addValue("organisasjonsnummer", upsertEnhetCmd.organisasjonsnummer)
			.addValue("navn", upsertEnhetCmd.navn)
			.addValue("overordnetEnhet", upsertEnhetCmd.overordnetEnhet)

		namedJdbcTemplate.update(sql, parameters)
	}

}
