package org.craftsmenlabs;

import mockit.Verifications;
import org.craftsmenlabs.gareth.ExperimentStorage;
import org.craftsmenlabs.gareth.GlueLineExecutor;
import org.craftsmenlabs.gareth.model.Experiment;

import java.util.ArrayList;
import java.util.List;

public class Captors {

    public static List<Experiment> experimentStorage_save(ExperimentStorage experimentStorage) {
        final List<Experiment> captor = new ArrayList<>();
        new Verifications() {{
            experimentStorage.save(withCapture(captor));
        }};
        return captor;
    }

    public static List<Experiment> glueLineExecutor_executeAssume(GlueLineExecutor glueLineExecutor) {
        final List<Experiment> captor = new ArrayList<>();
        new Verifications() {{
            glueLineExecutor.executeAssume(withCapture(captor));
        }};
        return captor;
    }
}