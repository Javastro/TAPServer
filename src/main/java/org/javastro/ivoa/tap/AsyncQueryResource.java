/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.javastro.ivoacore.common.ServiceLocator;
import org.javastro.ivoacore.tap.TAPJobSpecification;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;
import org.javastro.ivoacore.uws.webapi.BaseUWSResource;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;

/**
 * Main Async TAP Query.
 * Created on 04/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@Tag(name="TAP Query", description = "the TAP query endpoints")
@ApplicationScoped
@Path("async")
public class AsyncQueryResource extends BaseUWSResource {


   @Inject
   TAPHelper tapHelper;

   @Override
   protected JobManager getJobManager() {
      return tapHelper.jobmanager;
   }

   @Override
   protected Response redirectToJob(String jobid)  {

      final UriBuilder urib = UriBuilder.fromUri(tapHelper.serviceLocator.serviceURI())
            .path("async");
      if (jobid != null && !jobid.isEmpty()) {
         urib.path(jobid);
      }
      return Response.seeOther(urib
            .build()).build();
   }


   //IMPL the two query endpoints are in different resources for routing purposes.
    @POST
    public Response async(@RestForm("QUERY") String query, @RestForm("LANG") String lang, @RestForm("RESPONSEFORMAT") String responseformat,
                          @RestForm("MAXREC") Long maxrec, @RestForm("RUNID") String runid,
                          @RestForm("UPLOAD") String upload, @Context UriInfo uriInfo) throws UWSException {
       BaseUWSJob job = tapHelper.jobmanager.createJob(new TAPJobSpecification(query,lang,responseformat,maxrec,runid,upload));
       return Response.seeOther(tapHelper.asyncJobUri(job.getID())).build();
    }

   @GET
   @Path("{jobid}/results/result")
   @Produces("application/x-votable+xml")
   public RestResponse<java.nio.file.Path> getVotable(@PathParam("jobid") String jobid) throws UWSException {
      final java.nio.file.Path path = tapHelper.getResultPath(jobid);
      return RestResponse.ResponseBuilder.ok(path)
            .header(HttpHeaders.CONTENT_DISPOSITION, "result.vot")
            .build();
   }


}
