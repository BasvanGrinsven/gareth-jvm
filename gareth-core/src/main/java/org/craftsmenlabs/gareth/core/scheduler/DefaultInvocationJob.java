package org.craftsmenlabs.gareth.core.scheduler;

import com.xeiam.sundial.Job;
import com.xeiam.sundial.JobContext;
import com.xeiam.sundial.exceptions.JobInterruptException;
import lombok.AccessLevel;
import lombok.Setter;
import org.craftsmenlabs.gareth.api.context.ExperimentPartState;
import org.craftsmenlabs.gareth.core.invoker.MethodDescriptor;
import org.craftsmenlabs.gareth.core.ExperimentEngineImpl;
import org.craftsmenlabs.gareth.core.context.ExperimentRunContextImpl;
import org.craftsmenlabs.gareth.core.invoker.MethodInvokerImpl;
import org.craftsmenlabs.gareth.core.observer.DefaultObserver;
import org.craftsmenlabs.gareth.core.storage.DefaultStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class DefaultInvocationJob extends Job {

    private static final Logger logger = LoggerFactory.getLogger(DefaultInvocationJob.class);

    @Setter(AccessLevel.PROTECTED) // Setter for testing purposes (Adding this method for test is more important)
    private JobContext jobContext;

    @Override
    public void doRun() throws JobInterruptException {
        final MethodInvokerImpl methodInvoker = getJobContext().getRequiredValue("methodInvoker");
        final ExperimentRunContextImpl runContext = getJobContext().getRequiredValue("experimentRunContext");
        final DefaultObserver observer = getJobContext().getRequiredValue("observer");
        final ExperimentEngineImpl experimentEngine = getJobContext().getRequiredValue("experimentEngine");
        try {
            logger.debug("Invoking assumption");
            runContext.setAssumeState(ExperimentPartState.RUNNING);
            String assumeGlueLine = runContext.getExperimentContext().getAssumeGlueLine();
            invoke(assumeGlueLine, methodInvoker, runContext.getExperimentContext().hasStorage(), runContext
                    .getExperimentContext().getAssume(), runContext.getStorage());
            runContext.setAssumeRun(LocalDateTime.now());
            runContext.setAssumeState(ExperimentPartState.FINISHED);
            if (null != runContext.getExperimentContext().getSuccess()) {
                logger.debug("Invoking success");
                runContext.setSuccessState(ExperimentPartState.RUNNING);
                invoke(runContext.getExperimentContext().getSuccessGlueLine(),
                        methodInvoker, runContext
                                .getExperimentContext().hasStorage(), runContext
                                .getExperimentContext().getSuccess(), runContext.getStorage());
                runContext.setSuccessRun(LocalDateTime.now());
                runContext.setSuccessState(ExperimentPartState.FINISHED);
            }
        } catch (final Exception e) {
            runContext.setAssumeState(ExperimentPartState.ERROR);
            if (null != runContext.getExperimentContext().getFailure()) {
                logger.debug("Invoking failure");
                runContext.setFailureState(ExperimentPartState.RUNNING);
                invoke(runContext.getExperimentContext().getFailureGlueLine(),
                        methodInvoker, runContext.getExperimentContext().hasStorage(), runContext
                                .getExperimentContext().getFailure(), runContext.getStorage());
                runContext.setFailureRun(LocalDateTime.now());
                runContext.setFailureState(ExperimentPartState.FINISHED);
            }
        } finally {
            observer.notifyApplicationStateChanged(experimentEngine);
        }
    }

    private void invoke(final String assumeGlueLine,
                        final MethodInvokerImpl methodInvoker,
                        final boolean storageRequired,
                        final MethodDescriptor methodDescriptor,
                        final DefaultStorage storage) {
        if (storageRequired) {
            methodInvoker.invoke(assumeGlueLine, methodDescriptor, storage);
        } else {
            methodInvoker.invoke(assumeGlueLine, methodDescriptor);
        }
    }

    @Override
    protected JobContext getJobContext() {
        JobContext returnJobContext;
        if (null != this.jobContext) {
            returnJobContext = this.jobContext;
        } else {
            returnJobContext = super.getJobContext();
        }
        return returnJobContext;
    }
}
