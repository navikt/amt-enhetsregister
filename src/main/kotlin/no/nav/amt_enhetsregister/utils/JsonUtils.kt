package no.nav.amt_enhetsregister.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonUtils {

	private val objectMapper = ObjectMapper()
		.registerKotlinModule()
		.registerModule(JavaTimeModule())
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	fun getObjectMapper(): ObjectMapper {
		return objectMapper
	}

	fun listCollectionType(clazz: Class<*>): CollectionType {
		return objectMapper.typeFactory.constructCollectionType(
			List::class.java,
			clazz
		)
	}

	fun toJsonString(any: Any): String {
		return objectMapper.writeValueAsString(any)
	}

}
