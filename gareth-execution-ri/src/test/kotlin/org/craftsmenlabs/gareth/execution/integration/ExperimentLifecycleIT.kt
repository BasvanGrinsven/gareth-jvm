package org.craftsmenlabs.gareth.execution.integration

import org.assertj.core.api.Assertions.assertThat
import org.craftsmenlabs.gareth.api.execution.ExecutionRequest
import org.craftsmenlabs.gareth.api.execution.ExecutionResult
import org.craftsmenlabs.gareth.api.execution.ExecutionStatus
import org.craftsmenlabs.gareth.api.execution.ExperimentRunEnvironment
import org.craftsmenlabs.gareth.api.model.Duration
import org.craftsmenlabs.gareth.execution.Application
import org.craftsmenlabs.gareth.execution.dto.ExperimentRunEnvironmentBuilder
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate
import java.net.URI

@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(Application::class), webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("Test")
class ExperimentLifecycleIT {

    val path = "http://localhost:8090/gareth/v1/"
    val template = RestTemplate()

    @Test
    fun testBaseline() {
        val request = createRequest("sale of fruit", ExperimentRunEnvironmentBuilder.createEmpty())
        assertThat(doPut("${path}baseline", request).status).isEqualTo(ExecutionStatus.RUNNING)
    }

    @Test
    fun testSuccessfulAssume() {
        val request = createRequest("sale of fruit has risen by 81 per cent", ExperimentRunEnvironmentBuilder.createEmpty())
        assertThat(doPut("${path}assume", request).status).isEqualTo(ExecutionStatus.SUCCESS)
    }

    @Test
    fun testFailedAssume() {
        val request = createRequest("sale of fruit has risen by 79 per cent", ExperimentRunEnvironmentBuilder.createEmpty())
        assertThat(doPut("${path}assume", request).status).isEqualTo(ExecutionStatus.FAILURE)
    }

    @Test
    fun testDuration() {
        val request = RequestEntity(createRequest("next Easter", ExperimentRunEnvironmentBuilder.createEmpty()), HttpMethod.PUT, URI("${path}time"))
        val response = template.exchange(request, Duration::class.java)
        assertThat(response.body.amount).isEqualTo(14400L)
    }

    @Test
    fun testSuccess() {
        val request = createRequest("send email to John", ExperimentRunEnvironmentBuilder.createEmpty())
        val environment = doPut("${path}success", request).environment
        assertThat(environment.items.find { it.key == "emailtext" }?.value).isEqualTo("sending mail to John")
    }

    @Test
    fun testFailure() {
        val request = createRequest("send email to Bob", ExperimentRunEnvironmentBuilder.createEmpty())
        assertThat(doPut("${path}failure", request).status).isEqualTo(ExecutionStatus.FAILURE)
    }


    fun createRequest(glueLine: String, environment: ExperimentRunEnvironment): ExecutionRequest {
        return ExecutionRequest(environment, glueLine)
    }

    fun doPut(path: String, dto: ExecutionRequest): ExecutionResult {
        val builder = RequestEntity.put(URI(path)).contentType(MediaType.APPLICATION_JSON).body(dto)
        val response = template.exchange(builder, ExecutionResult::class.java)
        assertThat(response.statusCode.is2xxSuccessful).isTrue()
        println(response.body)
        return response.body
    }

}

