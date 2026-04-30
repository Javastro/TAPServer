/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap.quarkus;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.tap.UWSService;
import org.javastro.ivoacore.common.ServiceLocator;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;
import org.javastro.ivoacore.uws.webapi.BaseUWSResource;

/*
 * Created on 06/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@Tag(name="UWS", description = "The IVOA standard UWS endpoints")
@ApplicationScoped
@Path("/async/")
public class UWSResource extends BaseUWSResource {
   @Inject
   JobManager  jobManager;

   @Inject
   ServiceLocator serviceLocator;

   @Inject
   UWSService uwsService;

   @Override
   protected JobManager getJobManager() {
      return jobManager;
   }

   @Override
   protected Response redirectToJob(String jobid)  {

      final UriBuilder urib = UriBuilder.fromUri(serviceLocator.serviceURI())
            .path("async");
      if (jobid != null && !jobid.isEmpty()) {
         urib.path(jobid);
      }
      return Response.seeOther(urib
            .build()).build();
   }

   @Override
   public Response setPhase(@PathParam("jobid") String jobid, @FormParam("PHASE") String phase) throws UWSException {
      ExecutionPhase newphase = uwsService.setPhase(jobid, phase);  // Use uwsService
      return redirectToJob(jobid);
   }

   @Override
   public Response deleteJob(@PathParam("jobid") String jobid) throws UWSException {
      boolean success = uwsService.deleteJob(jobid);  // Use uwsService
      if (success) {
         return redirectToJob(null);
      } else {
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                 .entity("Failed to delete job " + jobid)
                 .build();
      }
   }
}
