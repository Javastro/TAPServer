/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.javastro.ivoacore.common.ServiceLocator;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;

import java.net.URI;

public abstract class BaseTAPResource {
   @Inject
   protected JobManager jobmanager;

   @Inject
   ServiceLocator serviceLocator;

   protected java.nio.file.Path getResultPath(String jobid) throws UWSException {
      //FIXME this needs to be refactored to be generalize - it knows too much about the internal workings - particularly that the result is stored in local file - that is also being exposed in the results job structure at the moment
      String res = jobmanager.getJobResults(jobid).stream().filter(r -> r.getId().equals("result")).findFirst().orElseThrow(() -> new UWSException("No result with id 'result'")).getValue();
      java.nio.file.Path path = java.nio.file.Path.of(res);
      return path;
   }

   protected URI asyncJobUri(String jobId) {
      return UriBuilder.fromUri(serviceLocator.serviceURI())
            .path("async")
            .path(jobId)
            .build();
   }

   protected Response errorResponse(Response.Status status, String message) {
      return Response.status(status)
            .type(MediaType.TEXT_PLAIN_TYPE)
            .entity(message)
            .build();
   }


}
