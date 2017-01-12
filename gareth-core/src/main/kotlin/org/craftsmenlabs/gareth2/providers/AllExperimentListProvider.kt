package org.craftsmenlabs.gareth2.providers

import org.craftsmenlabs.gareth2.ExperimentStorage
import org.craftsmenlabs.gareth2.model.Experiment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

@Service
class AllExperimentListProvider @Autowired constructor(private val experimentStorage: ExperimentStorage) {

    private val behaviourSubject = BehaviorSubject.create<List<Experiment>>()

    init {
        behaviourSubject.doOnSubscribe {
            publishAllExperiments()
        }
    }

    fun publishAllExperiments() {
        val allExperiments = experimentStorage.loadAllExperiments()
        behaviourSubject.onNext(allExperiments)
    }

    val observable = behaviourSubject
            .subscribeOn(Schedulers.computation())
}