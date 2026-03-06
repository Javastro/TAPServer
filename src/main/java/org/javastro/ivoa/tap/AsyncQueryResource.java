/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.javastro.ivoacore.tap.TAPJobSpecification;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

/**
 * Main Async TAP Query.
 * Created on 04/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@Tag(name="TAP Query", description = "the TAP query endpoints")
@ApplicationScoped
@Path("/async")
public class AsyncQueryResource {
   //IMPL the two query endpoints are in different resources for routing purposes.

   @Inject
   JobManager jobmanager;

    @POST
    public Response async(@RestForm String query, @RestForm String lang, @RestForm String responseformat, @RestForm Long maxrec, @RestForm String runid,
                          @RestForm String upload, @Context UriInfo uriInfo) throws UWSException {
       BaseUWSJob job = jobmanager.createJob(new TAPJobSpecification(query,lang,responseformat,maxrec,runid,upload));
       return Response.seeOther(uriInfo.getAbsolutePathBuilder()
             .path(job.getID()).build()).build();
    }
}
