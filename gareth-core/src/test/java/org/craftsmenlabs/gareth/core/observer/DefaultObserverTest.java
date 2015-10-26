package org.craftsmenlabs.gareth.core.observer;

import org.craftsmenlabs.gareth.api.ExperimentEngine;
import org.craftsmenlabs.gareth.api.exception.GarethStateWriteException;
import org.craftsmenlabs.gareth.api.listener.ExperimentStateChangeListener;
import org.craftsmenlabs.gareth.core.expect.Expect;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by hylke on 26/10/15.
 */
public class DefaultObserverTest {

    @Mock
    private ExperimentEngine mockExperimentEngine;

    @Mock
    private ExperimentStateChangeListener experimentStateChangeListener;

    @Mock
    private ExperimentStateChangeListener secondExperimentStateChangeListener;

    private DefaultObserver defaultObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        defaultObserver = new DefaultObserver();
        defaultObserver.registerExperimentStateChangeListener(experimentStateChangeListener);
    }

    @Test
    public void testNotifyApplicationStateChanged() throws Exception {
        defaultObserver.notifyApplicationStateChanged(mockExperimentEngine);
        verify(experimentStateChangeListener).onChange(mockExperimentEngine);
    }

    @Test
    public void testNotifyApplicationStateChangedWithException() throws Exception {
        doThrow(GarethStateWriteException.class).when(experimentStateChangeListener).onChange(mockExperimentEngine);

        try {
            defaultObserver.notifyApplicationStateChanged(mockExperimentEngine);
        }catch (final Exception e){
            fail("Should not reach this point");
        }
    }

    @Test
    public void testRegisterExperimentStateChangeListener() throws Exception {
        defaultObserver.registerExperimentStateChangeListener(secondExperimentStateChangeListener);
        defaultObserver.notifyApplicationStateChanged(mockExperimentEngine);
        verify(experimentStateChangeListener).onChange(mockExperimentEngine);
        verify(secondExperimentStateChangeListener).onChange(mockExperimentEngine);
    }
}