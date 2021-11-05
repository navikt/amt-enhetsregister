package no.nav.amt_enhetsregister.repository

import no.nav.amt_enhetsregister.repository.type.OppdaterEnhetJobb
import no.nav.amt_enhetsregister.repository.type.OppdaterEnhetJobbStatus
import no.nav.amt_enhetsregister.repository.type.OppdaterEnhetJobbType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.time.ZoneId

@Repository
class OppdaterEnhetJobbRepository(private val jdbcTemplate: JdbcTemplate) {

	companion object Table {
		const val TABLE_NAME = "oppdater_enhet_jobb"

		const val ID = "id"
		const val CURRENT_PAGE = "current_page"
		const val PAGE_SIZE = "page_size"
		const val TOTAL_PAGES = "total_pages"
		const val TYPE = "type"
		const val STATUS = "status"
		const val FINISHED_AT = "finished_at"
		const val PAUSED_AT = "paused_at"
	}

	val rowMapper =
		RowMapper { rs, _ ->
			OppdaterEnhetJobb(
				id = rs.getInt(ID),
				currentPage = rs.getInt(CURRENT_PAGE),
				pageSize = rs.getInt(PAGE_SIZE),
				totalPages = rs.getInt(TOTAL_PAGES),
				type = OppdaterEnhetJobbType.valueOf(rs.getString(TYPE)),
				status = OppdaterEnhetJobbStatus.valueOf(rs.getString(STATUS)),
				finishedAt = rs.getTimestamp(FINISHED_AT).toLocalDateTime().atZone(ZoneId.systemDefault()),
				pausedAt = rs.getTimestamp(PAUSED_AT).toLocalDateTime().atZone(ZoneId.systemDefault())
			)
		}

	fun startJobb(type: OppdaterEnhetJobbType): OppdaterEnhetJobb {
		val sql = """
			INSERT INTO $TABLE_NAME ($ID, $TYPE, $STATUS) VALUES (?, ?::oppdater_enhet_jobb_type, ?::oppdater_enhet_jobb_status)
		""".trimIndent()

		val id = jdbcTemplate.query("SELECT nextval('$TABLE_NAME.$ID')") { rs, _ -> rs.getInt(ID) }.first()

		jdbcTemplate.update(sql, id, type.name, OppdaterEnhetJobbStatus.IN_PROGRESS.name)

		return hentJobb(id)
	}

	fun hentJobb(jobbId: Int): OppdaterEnhetJobb {
		val sql = """
			SELECT * FROM $TABLE_NAME WHERE $ID = ? LIMIT 1
		""".trimIndent()

		return jdbcTemplate.query(sql, rowMapper, jobbId).first()
	}

	fun oppdaterProgresjon(jobbId: Int, currentPage: Int, pageSize: Int, totalPages: Int) {
		val sql = """
			UPDATE $TABLE_NAME SET $STATUS = ?::oppdater_enhet_jobb_status,
			$CURRENT_PAGE = ?, $PAGE_SIZE = ?, $TOTAL_PAGES = ? WHERE $ID = ?
		""".trimIndent()

		jdbcTemplate.update(sql, OppdaterEnhetJobbStatus.IN_PROGRESS.name, currentPage, pageSize, totalPages, jobbId)
	}

	fun markerJobbPauset(jobbId: Int) {
		val sql = """
			UPDATE $TABLE_NAME SET $STATUS = ?::oppdater_enhet_jobb_status, $PAUSED_AT = CURRENT_TIMESTAMP WHERE $ID = ?
		""".trimIndent()

		jdbcTemplate.update(sql, OppdaterEnhetJobbStatus.PAUSED.name, jobbId)
	}

	fun fullforJobb(jobbId: Int) {
		val sql = """
			UPDATE $TABLE_NAME SET $STATUS = ?::oppdater_enhet_jobb_status, $FINISHED_AT = CURRENT_TIMESTAMP WHERE $ID = ?
		""".trimIndent()

		jdbcTemplate.update(sql, OppdaterEnhetJobbStatus.COMPLETED.name, jobbId)
	}

	fun hentSisteJobb(type: OppdaterEnhetJobbType): OppdaterEnhetJobb? {
		val sql = """
			SELECT * FROM $TABLE_NAME WHERE $TYPE = ?::oppdater_enhet_jobb_status ORDER BY $ID DESC LIMIT 1
		""".trimIndent()

		return jdbcTemplate.query(sql, rowMapper, type.name).firstOrNull()
	}

}
