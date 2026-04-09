/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap.quarkus;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.javastro.ivoacore.common.ServiceLocator;
import org.javastro.ivoacore.uws.JobManager;
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
}
