package no.nav.amt_enhetsregister

import no.nav.amt_enhetsregister.test_utils.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalManagementPort
import org.springframework.http.HttpStatus
import org.springframework.web.util.UriComponentsBuilder

class ActuatorTest(
    @LocalManagementPort private val managementPort: Int,
    private val restTemplate: TestRestTemplate,
) : IntegrationTest() {
    @ParameterizedTest(name = "{0} probe skal returnere OK og status = UP")
    @ValueSource(strings = ["liveness", "readiness"])
    fun probe_skal_returnere_OK_og_status_UP(probeName: String) {
        val uri =
            UriComponentsBuilder
                .fromUriString("http://localhost:{port}/internal/health/{probeName}")
                .buildAndExpand(managementPort, probeName)
                .toUri()

        val response = restTemplate.getForEntity(uri, String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("{\"status\":\"UP\"}", response.body)
    }

    @Test
    fun `Prometheus-endepunktet skal returnere OK`() {
        val uri =
            UriComponentsBuilder
                .fromUriString("http://localhost:{port}/internal/prometheus")
                .buildAndExpand(managementPort)
                .toUri()

        val response = restTemplate.getForEntity(uri, String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `Metrics-endepunktet skal returnere NOT_FOUND`() {
        val uri =
            UriComponentsBuilder
                .fromUriString("http://localhost:{port}/internal/metrics")
                .buildAndExpand(managementPort)
                .toUri()

        val response = restTemplate.getForEntity(uri, String::class.java)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}
