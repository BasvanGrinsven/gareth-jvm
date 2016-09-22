package org.craftsmenlabs.gareth.rest.v1.resource;

import org.craftsmenlabs.gareth.api.exception.GarethUnknownExperimentException;
import org.craftsmenlabs.gareth.core.ExperimentEngineImpl;
import org.craftsmenlabs.gareth.core.context.ExperimentContextImpl;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/experiments-rerun")
public class ExperimentRerunResource {

    @Inject
    private ExperimentEngineImpl experimentEngine;

    @GET
    @Path("{hash}")
    public Response rerunExperiment(final @PathParam("hash") String hash) {
        Response response = null;
        try {
            final ExperimentContextImpl experimentContext = experimentEngine.findExperimentContextForHash(hash);
            experimentEngine.planExperimentContext(experimentContext);
            response = Response.accepted().build();
        } catch (final GarethUnknownExperimentException e) {
            response = Response.status(Response.Status.NOT_FOUND).build();
        }
        return response;
    }
}
