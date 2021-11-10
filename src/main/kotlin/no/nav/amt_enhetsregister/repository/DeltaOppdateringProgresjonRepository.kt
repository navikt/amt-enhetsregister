package no.nav.amt_enhetsregister.repository

import no.nav.amt_enhetsregister.repository.type.DeltaEnhetOppdateringProgresjon
import no.nav.amt_enhetsregister.repository.type.EnhetType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.time.ZoneId

@Repository
class DeltaOppdateringProgresjonRepository(private val jdbcTemplate: JdbcTemplate) {

	private companion object Table {
		const val TABLE_NAME = "delta_enhet_oppdatering"

		const val OPPDATERING_ID = "oppdatering_id"
		const val ENHET_TYPE = "enhet_type"
		const val SISTE_OPPDATERING = "siste_oppdatering"
	}

	val rowMapper =
		RowMapper { rs, _ ->
			DeltaEnhetOppdateringProgresjon(
				oppdateringId = rs.getInt(OPPDATERING_ID),
				enhetType = EnhetType.valueOf(rs.getString(ENHET_TYPE)),
				sisteOppdatering = rs.getTimestamp(SISTE_OPPDATERING).toLocalDateTime().atZone(ZoneId.systemDefault())
			)
		}

	fun hentOppdateringProgresjon(enhetType: EnhetType): DeltaEnhetOppdateringProgresjon {
		val sql = """
			SELECT * FROM $TABLE_NAME WHERE $ENHET_TYPE = ?::enhet_type
		""".trimIndent()

		return jdbcTemplate.query(sql, rowMapper, enhetType.name).first()
	}

	fun oppdaterProgresjon(enhetType: EnhetType, oppdateringId: Int) {
		val sql = """
			UPDATE $TABLE_NAME SET $OPPDATERING_ID = ?, $SISTE_OPPDATERING = CURRENT_TIMESTAMP WHERE $ENHET_TYPE = ?::enhet_type
		""".trimIndent()

		jdbcTemplate.update(sql, oppdateringId, enhetType.name)
	}

}
