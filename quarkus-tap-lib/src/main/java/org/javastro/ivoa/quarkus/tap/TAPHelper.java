/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.quarkus.tap;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.javastro.ivoacore.common.ServiceLocator;
import org.javastro.ivoacore.uws.JobManager;
import org.javastro.ivoacore.uws.UWSException;

import java.net.URI;
import java.util.Set;


public class TAPHelper {
//TODO not sure if this could be factored away - or combined with config ain another way


   public static final Set<String> ALLOWED_PARAMS = Set.of(
      "QUERY",
      "LANG",
      "RESPONSEFORMAT",
      "MAXREC",
      "RUNID",
      "UPLOAD"
   );
   private final JobManager jobmanager;

   private final ServiceLocator serviceLocator;

   public TAPHelper(JobManager jobmanager, ServiceLocator serviceLocator) {
      this.jobmanager = jobmanager;
      this.serviceLocator = serviceLocator;
   }

   public JobManager getJobmanager() {
      return jobmanager;
   }

   public ServiceLocator getServiceLocator() {
      return serviceLocator;
   }
   public java.nio.file.Path getResultPath(String jobid) throws UWSException {
      //FIXME this needs to be refactored to be generalize - it knows too much about the internal workings - particularly that the result is stored in local file - that is also being exposed in the results job structure at the moment
      String res = jobmanager.getJobResults(jobid).stream().filter(r -> r.getId().equals("result")).findFirst().orElseThrow(() -> new UWSException("No result with id 'result'")).getValue();
      java.nio.file.Path path = java.nio.file.Path.of(res);
      return path;
   }

   public URI asyncJobUri(String jobId) {
      return UriBuilder.fromUri(serviceLocator.serviceURI())
            .path("async")
            .path(jobId)
            .build();
   }

   public Response errorResponse(Response.Status status, String message) {
      return Response.status(status)
            .type(MediaType.TEXT_PLAIN_TYPE)
            .entity(message)
            .build();
   }

   public Set<String>  unknownParameters(UriInfo uriInfo) {
      return uriInfo.getQueryParameters().keySet().stream().filter(param -> !ALLOWED_PARAMS.contains(param)).collect(java.util.stream.Collectors.toSet());
   }

   public boolean checkUnknownParameters(UriInfo uriInfo) throws UWSException {
      Set<String> unknownParams = unknownParameters(uriInfo);
      if (!unknownParams.isEmpty()) {
         throw new UWSException("Unknown parameters: " + String.join(", ", unknownParams)); //TODO perhaps change to different exception
      }
      return true;
   }
}
