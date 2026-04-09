/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.javastro.ivoacore.tap.TAPJobSpecification;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.UWSException;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;

/**
 * Main Async TAP Query.
 * Created on 04/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@Tag(name="TAP Query", description = "the TAP query endpoints")
@ApplicationScoped
@Path("/async")
public class AsyncQueryResource extends BaseTAPResource {
   //IMPL the two query endpoints are in different resources for routing purposes.
    @POST
    public Response async(@RestForm String query, @RestForm String lang, @RestForm String responseformat, @RestForm Long maxrec, @RestForm String runid,
                          @RestForm String upload, @Context UriInfo uriInfo) throws UWSException {
       BaseUWSJob job = jobmanager.createJob(new TAPJobSpecification(query,lang,responseformat,maxrec,runid,upload));
       return Response.seeOther(asyncJobUri(job.getID())).build();
    }

   @GET
   @Path("/{jobid}/results/result")
   @Produces("application/x-votable+xml")
   public RestResponse<java.nio.file.Path> getVotable(@PathParam("jobid") String jobid) throws UWSException {
      final java.nio.file.Path path = getResultPath(jobid);
      return RestResponse.ResponseBuilder.ok(path)
            .header(HttpHeaders.CONTENT_DISPOSITION, "result.vot")
            .build();
   }

}
