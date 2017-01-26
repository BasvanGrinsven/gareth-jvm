package org.craftsmenlabs.gareth2.client

import org.assertj.core.api.Assertions.assertThat
import org.craftsmenlabs.gareth.api.execution.ItemType
import org.craftsmenlabs.gareth.api.model.GlueLineType
import org.craftsmenlabs.gareth2.model.Experiment
import org.craftsmenlabs.gareth2.model.ExperimentDetails
import org.craftsmenlabs.gareth2.model.ExperimentResults
import org.craftsmenlabs.gareth2.model.ExperimentTiming
import org.junit.Test
import java.time.LocalDateTime


class ExecutionRequestFactoryTest {

    val factory = ExecutionRequestFactory()

    private val experiment: Experiment = Experiment(
            details = ExperimentDetails("B", "A", "T", "S", "F", 42, "UUID"),
            timing = ExperimentTiming(created = LocalDateTime.now()),
            results = ExperimentResults(false),
            environment = mapOf(Pair("age", 42), Pair("married", true), Pair("name", "John"), Pair("rate", 12.5))
    )

    @Test
    fun shouldcreateEnvironmentForAssume() {
        val request = factory.createForGluelineType(GlueLineType.ASSUME, experiment)
        assertThat(request.glueLine).isEqualTo("A")
    }

    @Test
    fun shouldcreateEnvironmentForBaseline() {
        val request = factory.createForGluelineType(GlueLineType.BASELINE, experiment)
        assertThat(request.glueLine).isEqualTo("B")
    }

    @Test
    fun shouldcreateEnvironmentForSuccess() {
        val request = factory.createForGluelineType(GlueLineType.SUCCESS, experiment)
        assertThat(request.glueLine).isEqualTo("S")
    }

    @Test
    fun shouldcreateEnvironmentForFailure() {
        val request = factory.createForGluelineType(GlueLineType.FAILURE, experiment)
        assertThat(request.glueLine).isEqualTo("F")
    }

    @Test
    fun shouldcreateEnvironmentForTime() {
        val request = factory.createForGluelineType(GlueLineType.TIME, experiment)
        assertThat(request.glueLine).isEqualTo("T")
    }

    @Test
    fun shouldcreateEnvironmentWithItemTypes() {
        val request = factory.createForGluelineType(GlueLineType.ASSUME, experiment)
        assertThat(request.environment.items.map { it.key }).containsExactly("age", "married", "name", "rate")
        assertThat(request.environment.items.map { it.value }).containsExactly("42", "true", "John", "12.5")
        assertThat(request.environment.items.map { it.itemType }).containsExactly(ItemType.LONG, ItemType.BOOLEAN, ItemType.STRING, ItemType.DOUBLE)
    }
}