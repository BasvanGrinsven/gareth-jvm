package org.craftsmenlabs.gareth2.monitors

import org.craftsmenlabs.gareth2.ExperimentStorage
import org.craftsmenlabs.gareth2.GlueLineExecutor
import org.craftsmenlabs.gareth2.model.Experiment
import org.craftsmenlabs.gareth2.model.ExperimentState
import org.craftsmenlabs.gareth2.providers.ExperimentProvider
import org.craftsmenlabs.gareth2.time.DateTimeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rx.Observable

@Service
class ExecuteAssumeMonitor @Autowired constructor(
        experimentProvider: ExperimentProvider,
        dateTimeService: DateTimeService,
        experimentStorage: ExperimentStorage,
        private val glueLineExecutor: GlueLineExecutor)
    : BaseMonitor(
        experimentProvider, dateTimeService, experimentStorage, ExperimentState.WAITING_FOR_ASSUME) {

    override fun extend(observable: Observable<Experiment>): Observable<Experiment> {
        return observable
                .map { it.apply { it.results.success = glueLineExecutor.executeAssumption(it) } }
                .map { it.apply { it.timing.assumeExecuted = dateTimeService.now() } }
    }
}