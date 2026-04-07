/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.javastro.ivoacore.tap.TAPJob;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;
import org.jspecify.annotations.NonNull;

import java.net.URI;

public abstract class BaseTAPResource {
   @Inject
   JobManager jobmanager;

   protected java.nio.file.@NonNull Path getResultPath(String jobid) throws UWSException {
      //FIXME this needs to be refactored to be generalize - it knows too much about the internal workings - particularly that the result is stored in local file - that is also being exposed in the results job structure at the moment
      String res = jobmanager.getJobResults(jobid).stream().filter(r -> r.getId().equals("result")).findFirst().orElseThrow(() -> new UWSException("No result with id 'result'")).getValue();
      java.nio.file.Path path = java.nio.file.Path.of(res);
      return path;
   }

   protected URI asyncJobUri(UriInfo uriInfo, String jobId) {
      return uriInfo.getBaseUriBuilder()
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
