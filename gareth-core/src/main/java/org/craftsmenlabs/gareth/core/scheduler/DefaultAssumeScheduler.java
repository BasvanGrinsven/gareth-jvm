package org.craftsmenlabs.gareth.core.scheduler;

import com.xeiam.sundial.SundialJobScheduler;
import org.craftsmenlabs.gareth.api.ExperimentEngine;
import org.craftsmenlabs.gareth.api.context.ExperimentRunContext;
import org.craftsmenlabs.gareth.api.exception.GarethInvocationException;
import org.craftsmenlabs.gareth.api.exception.GarethUnknownDefinitionException;
import org.craftsmenlabs.gareth.api.invoker.MethodInvoker;
import org.craftsmenlabs.gareth.api.observer.Observer;
import org.craftsmenlabs.gareth.api.scheduler.AssumeScheduler;
import org.craftsmenlabs.gareth.core.invoker.MethodInvokerImpl;
import org.craftsmenlabs.gareth.core.reflection.DefinitionFactory;
import org.craftsmenlabs.gareth.core.reflection.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;


public class DefaultAssumeScheduler implements AssumeScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAssumeScheduler.class);

    private final MethodInvoker methodInvoker;
    private final boolean ignoreInvocationExceptions;
    private final Observer observer;


    private DefaultAssumeScheduler(final Builder builder) {
        this.methodInvoker = builder.methodInvoker;
        this.ignoreInvocationExceptions = builder.ignoreInvocationExceptions;
        this.observer = builder.observer;
        SundialJobScheduler.startScheduler();
    }

    @Override
    public void schedule(final ExperimentRunContext experimentContext, final ExperimentEngine experimentEngine) {
        final Duration time = experimentContext.getExperimentContext().getTime();
        final Calendar now = new GregorianCalendar();
        now.add(Calendar.MILLISECOND, new Long(time.toMillis()).intValue());
        try {

            // Job params
            final Map<String, Object> jobParams = new HashMap<>();
            jobParams.put("experimentRunContext", experimentContext);
            jobParams.put("methodInvoker", methodInvoker);
            jobParams.put("observer", observer);
            jobParams.put("experimentEngine", experimentEngine);

            final String jobName = experimentContext.getExperimentContext().getExperimentName() + "-" + new Random()
                    .nextInt();
            final String triggerName = jobName + "-trigger";


            SundialJobScheduler.addJob(jobName, DefaultInvocationJob.class.getName(), jobParams, false);
            safeThreadSleep(1); // Seems like SundialJobScheduler needs a minimum amount of delay between these two calls.
            // Otherwise ExperimentEngineImplIT will fail. TODO: Figure out why
            SundialJobScheduler.addSimpleTrigger(triggerName, jobName, 0, 1, now.getTime(), null);
        } catch (final GarethUnknownDefinitionException | GarethInvocationException e) {
            System.out.println("############ Gareth Exception ###########");
            logger.error("Problem during assumption invocation", e);
            if (!ignoreInvocationExceptions) {
                throw e;
            }
        }
    }

    private void safeThreadSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class Builder {
        private final org.craftsmenlabs.gareth.api.observer.Observer observer;
        private ReflectionHelper reflectionHelper;
        private MethodInvoker methodInvoker;
        private boolean ignoreInvocationExceptions;
        private DefinitionFactory customDefinitionFactory;

        public Builder(final Observer observer) {
            this.observer = observer;
        }

        public Builder setIgnoreInvocationExceptions(final boolean ignoreInvocationExceptions) {
            this.ignoreInvocationExceptions = ignoreInvocationExceptions;
            return this;
        }

        public Builder setMethodInvoker(final MethodInvoker methodInvoker) {
            this.methodInvoker = methodInvoker;
            return this;
        }

        public Builder addCustomDefinitionFactory(final DefinitionFactory customDefinitionFactory) {
            this.customDefinitionFactory = customDefinitionFactory;
            return this;
        }

        public AssumeScheduler build() {
            if (null == observer) {
                throw new IllegalStateException("Observer cannot be null");
            }

            reflectionHelper = new ReflectionHelper(customDefinitionFactory);

            if (methodInvoker == null) {
                methodInvoker = new MethodInvokerImpl(reflectionHelper);
            }

            return new DefaultAssumeScheduler(this);
        }
    }

}
