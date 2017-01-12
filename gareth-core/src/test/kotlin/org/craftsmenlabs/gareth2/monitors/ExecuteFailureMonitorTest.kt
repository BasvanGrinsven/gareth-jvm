package org.craftsmenlabs.gareth2.monitors

import mockit.Expectations
import mockit.Injectable
import mockit.Verifications
import org.assertj.core.api.Assertions
import org.craftsmenlabs.gareth2.ExperimentStorage
import org.craftsmenlabs.gareth2.GlueLineExecutor
import org.craftsmenlabs.gareth2.model.Experiment
import org.craftsmenlabs.gareth2.model.ExperimentDetails
import org.craftsmenlabs.gareth2.model.ExperimentResults
import org.craftsmenlabs.gareth2.model.ExperimentTiming
import org.craftsmenlabs.gareth2.providers.ExperimentProvider
import org.craftsmenlabs.gareth2.time.DateTimeService
import org.junit.Before
import org.junit.Test
import rx.lang.kotlin.toObservable
import java.time.LocalDateTime

class ExecuteFailureMonitorTest {

    val localDateTime1 = LocalDateTime.now().minusHours(1)      //created
    val localDateTime2 = LocalDateTime.now().minusHours(2)      //ready
    val localDateTime3 = LocalDateTime.now().minusHours(3)      //started
    val localDateTime4 = LocalDateTime.now().minusHours(4)      //waitingForBaseline
    val localDateTime5 = LocalDateTime.now().minusHours(5)      //baselineExecuted
    val localDateTime6 = LocalDateTime.now().minusHours(6)      //waitingForAssume
    val localDateTime7 = LocalDateTime.now().minusHours(7)      //assumeExecuted
    val localDateTime8 = LocalDateTime.now().minusHours(8)      //waitingFinalizing
    val localDateTime9 = LocalDateTime.now().minusHours(9)      //finalizingExecuted
    val localDateTime10 = LocalDateTime.now().minusHours(10)    //
    val localDateTime11 = LocalDateTime.now().minusHours(11)    //
    val localDateTime12 = LocalDateTime.now().minusHours(12)    //
    val localDateTime13 = LocalDateTime.now().minusHours(13)    //
    val localDateTime14 = LocalDateTime.now().minusHours(14)    //
    val localDateTime15 = LocalDateTime.now().minusHours(15)    //
    val localDateTime16 = LocalDateTime.now().minusHours(16)    //
    val localDateTime17 = LocalDateTime.now().minusHours(17)    //
    val localDateTime18 = LocalDateTime.now().minusHours(18)    //

    @Injectable
    lateinit var experimentProvider: ExperimentProvider

    @Injectable
    lateinit var dateTimeService: DateTimeService

    @Injectable
    lateinit var glueLineExecutor: GlueLineExecutor

    @Injectable
    lateinit var experimentStorage: ExperimentStorage

    lateinit var monitor: ExecuteFailureMonitor

    @Before
    fun setUp() {
        monitor = ExecuteFailureMonitor(experimentProvider, dateTimeService, experimentStorage, glueLineExecutor)
    }

    @Test
    fun shouldOnlyOperateOnStartedExperiments() {
        val details = ExperimentDetails("baseline", "assumption", "time", "success", "failure", 111, "id")
        val timingFinalisationExecuted = ExperimentTiming(
                localDateTime1,
                localDateTime2,
                localDateTime3,
                localDateTime4,
                localDateTime5,
                localDateTime6,
                localDateTime7,
                localDateTime8)
        val timingCompleted = ExperimentTiming(
                localDateTime9,
                localDateTime10,
                localDateTime11,
                localDateTime12,
                localDateTime13,
                localDateTime14,
                localDateTime15,
                localDateTime16,
                localDateTime17
        )

        val failResults = ExperimentResults()
        failResults.success = false
        val successResults = ExperimentResults()
        successResults.success = true

        val failedExperimentWaitingForFinalisation = Experiment(details, timingFinalisationExecuted, failResults)
        val succeededExperimentWaitingForFinalisation = Experiment(details, timingFinalisationExecuted, successResults)
        val experimentFinalisationExecuted = Experiment(details, timingCompleted, successResults)
        val experiments = listOf(failedExperimentWaitingForFinalisation, succeededExperimentWaitingForFinalisation, experimentFinalisationExecuted)

        object : Expectations() {
            init {
                experimentProvider.observable
                result = experiments.toObservable()

                dateTimeService.now()
                result = localDateTime18
            }
        }

        monitor.start();

        object : Verifications() {
            init {
                glueLineExecutor.executeFailure(succeededExperimentWaitingForFinalisation)
                times = 0

                glueLineExecutor.executeFailure(failedExperimentWaitingForFinalisation)
                times = 1

                glueLineExecutor.executeFailure(experimentFinalisationExecuted)
                times = 0

                experimentStorage.save(succeededExperimentWaitingForFinalisation)
                times = 0

                experimentStorage.save(failedExperimentWaitingForFinalisation)
                times = 1

                experimentStorage.save(experimentFinalisationExecuted)
                times = 0
            }
        }

        Assertions.assertThat(failedExperimentWaitingForFinalisation.timing.finalizingExecuted).isSameAs(localDateTime18)
    }
}