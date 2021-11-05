package no.nav.amt_enhetsregister.repository

import no.nav.amt_enhetsregister.repository.type.Enhet
import no.nav.amt_enhetsregister.repository.type.UpsertEnhetCmd
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement

@Repository
class EnhetRepository(private val jdbcTemplate: JdbcTemplate) {

	companion object Table {
		const val TABLE_NAME = "enhet"

		const val ID = "id"
		const val ORGANISASJONSNUMMER = "organisasjonsnummer"
		const val OVERORDNET_ENHET = "overordnet_enhet"
		const val NAVN = "navn"
		const val UPDATED_AT = "updated_at"
	}

	val rowMapper =
		RowMapper { rs, _ ->
			Enhet(
				id = rs.getInt(ID),
				organisasjonsnummer = rs.getString(ORGANISASJONSNUMMER),
				navn = rs.getString(NAVN),
				overordnetEnhet = rs.getString(OVERORDNET_ENHET)
			)
		}

	fun upsertEnheter(enheter: List<UpsertEnhetCmd>) {
		val sql = """
			INSERT INTO $TABLE_NAME ($ORGANISASJONSNUMMER, $NAVN, $OVERORDNET_ENHET) VALUES (?, ?, ?)
			ON CONFLICT ($ORGANISASJONSNUMMER)
			DO UPDATE SET $NAVN = EXCLUDED.$NAVN, $OVERORDNET_ENHET = EXCLUDED.$OVERORDNET_ENHET, $UPDATED_AT = CURRENT_TIMESTAMP
		""".trimIndent()

		jdbcTemplate.batchUpdate(
			sql,
			object : BatchPreparedStatementSetter {
				override fun setValues(ps: PreparedStatement, i: Int) {
					val enhet = enheter.get(i)

					ps.setString(1, enhet.organisasjonsnummer)
					ps.setString(2, enhet.navn)
					ps.setString(3, enhet.overordnetEnhet)
				}

				override fun getBatchSize(): Int {
					return enheter.size
				}
			}
		)
	}

	fun hentEnhet(organisasjonsnummer: String): Enhet? {
		val sql = """
			SELECT * FROM $TABLE_NAME WHERE $ORGANISASJONSNUMMER = ? LIMIT 1
		""".trimIndent()

		return jdbcTemplate.query(sql, rowMapper, organisasjonsnummer).firstOrNull()
	}

}
