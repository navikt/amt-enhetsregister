package no.nav.enhetsregister.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.LocalDateTime
import java.time.Year

@JsonTest
class ObjectMapperTest {

	@Autowired
	private lateinit var objectMapper: ObjectMapper

	@Test
	fun `skal serialisere og deserialisere LocalDateTime`() {
		val expected = SampleDto(name = "~name~", timestamp = now)

		val json = objectMapper.writeValueAsString(expected)
		val deserialized = objectMapper.readValue<SampleDto>(json)

		assertEquals(expected, deserialized)
	}

	@Test
	fun `skal deserialisere uten feil nar JSON inneholder ukjent felt`() {
		val expected = SampleDto(name = "~name~", timestamp = now)

		val deserialized = objectMapper.readValue<SampleDto>(jsonWithUnknownPropertyInTest)

		assertEquals(expected, deserialized)
	}

	companion object {
		private val currentYear = Year.now().value
		private val now: LocalDateTime = LocalDateTime.of(currentYear, 12, 25, 10, 30)

		private val jsonWithUnknownPropertyInTest = """{
			"name": "~name~",
			"timestamp": "$currentYear-12-25T10:30:00",
			"unknown-prop": "~unknown-prop~"
		}"""
	}

	private data class SampleDto(
		val name: String,
		val timestamp: LocalDateTime
	)
}
