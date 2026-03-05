/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.javastro.ivoacore.tap.TAPJobSpecification;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

/**
 * Main TAP Query.
 * Created on 04/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@Tag(name="TAP Query", description = "the TAP query endpoints")
public class QueryResource {

   @Inject
   JobManager jobmanager;

    @Path("/sync")
    @GET
    public Response sync(@RestQuery String query,  @RestQuery String lang, @RestQuery String responseformat, @RestQuery Long maxrec, @RestQuery String runid,
                         @RestQuery String upload) {
       //TODO create a job and run it in a way that allows for it to be interrupted after shortish time, so sync request times out, but allows job to continue running - return error that indicates where the job is....
       return Response.status(500,"Not yet implemented").build();
    }

    @Path("/async")
    @POST
    public Response async(@RestForm String query, @RestForm String lang, @RestForm String responseformat, @RestForm Long maxrec, @RestForm String runid,
                          @RestForm String upload) throws UWSException {
       BaseUWSJob job = jobmanager.createJob(new TAPJobSpecification(query,lang,responseformat,maxrec,runid,upload));
      return Response.ok().build();
    }
}
